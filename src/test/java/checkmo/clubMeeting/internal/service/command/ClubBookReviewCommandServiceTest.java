package checkmo.clubMeeting.internal.service.command;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.entity.ClubMeetingActor;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.BookReviewRepository;
import checkmo.clubMeeting.internal.service.query.ClubBookReviewQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.BookReviewCreate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClubBookReviewCommandServiceTest {

    private static final Long CLUB_ID = 1L;
    private static final Long MEETING_ID = 2L;
    private static final Long REVIEW_ID = 3L;
    private static final Long MEMBER_ID = 4L;
    private static final Long CLUB_MEMBER_ID = 5L;

    @Mock
    private ClubManagementAPI clubManagementAPI;
    @Mock
    private ClubMeetingQueryService clubMeetingQueryService;
    @Mock
    private ClubBookReviewQueryService clubBookReviewQueryService;
    @Mock
    private BookReviewRepository bookReviewRepository;
    @InjectMocks
    private ClubBookReviewCommandService service;

    private Meeting meeting;

    @BeforeEach
    void setUp() {
        meeting = meetingWithSumRate(0);
    }

    @Test
    void 한줄평을_모임에_연결하고_별점_합계를_더한_상태로_저장한다() {
        when(clubManagementAPI.validateAndFetchActiveClubMemberId(CLUB_ID, MEMBER_ID))
                .thenReturn(CLUB_MEMBER_ID);
        when(clubMeetingQueryService.validateMeeting(CLUB_ID, MEETING_ID)).thenReturn(meeting);
        ArgumentCaptor<BookReview> captor = ArgumentCaptor.forClass(BookReview.class);

        service.createBookReview(CLUB_ID, MEETING_ID, MEMBER_ID, request("좋았어요", 4.5));

        verify(bookReviewRepository).save(captor.capture());
        BookReview saved = captor.getValue();
        assertSoftly(softly -> {
            softly.assertThat(saved.getDescription()).isEqualTo("좋았어요");
            softly.assertThat(saved.getRate()).isEqualTo(4.5);
            softly.assertThat(saved.getClubMemberId()).isEqualTo(CLUB_MEMBER_ID);
            softly.assertThat(saved.getMemberId()).isEqualTo(MEMBER_ID);
            softly.assertThat(saved.getMeeting()).isSameAs(meeting);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.5);
            softly.assertThat(meeting.calculateAverageRate()).isEqualTo(4.5);
        });
    }

    @Test
    void 같은_별점으로_수정하면_내용만_바꾸고_별점_합계는_유지한다() {
        meeting = meetingWithSumRate(0.0);
        BookReview review = Mockito.spy(review("이전", 4.0, CLUB_MEMBER_ID));
        meeting.addBookReview(review);
        MembershipInfo ownerMembership = allowOwner(review);

        service.updateBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID, request("수정", 4.0));

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("수정");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
        });
        verifyUpdateOrder(
                ownerMembership,
                review,
                new ClubMeetingActor(CLUB_MEMBER_ID, false),
                "수정",
                4.0
        );
        verifyNoInteractions(bookReviewRepository);
    }

    @Test
    void 별점_수정은_기존_별점을_차감한_뒤_review를_변경하고_새_별점을_더한다() {
        meeting = meetingWithSumRate(-9.0);
        BookReview review = Mockito.spy(review("이전", 4.0, CLUB_MEMBER_ID));
        meeting.addBookReview(review);
        meeting.addBookReview(review("다른 한줄평", 6.0, 99L));
        MembershipInfo ownerMembership = allowOwner(review);

        service.updateBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID, request("수정", 2.0));

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("수정");
            softly.assertThat(review.getRate()).isEqualTo(2.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(8.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
        });
        verifyUpdateOrder(
                ownerMembership,
                review,
                new ClubMeetingActor(CLUB_MEMBER_ID, false),
                "수정",
                2.0
        );
        verifyNoInteractions(bookReviewRepository);
    }

    @Test
    void 운영진은_다른_회원의_한줄평을_수정할_수_있다() {
        meeting = meetingWithSumRate(0.0);
        BookReview review = Mockito.spy(review("기존", 4.0, 99L));
        meeting.addBookReview(review);
        MembershipInfo staffMembership = allowStaff(review);

        service.updateBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID, request("운영진 수정", 2.0));

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("운영진 수정");
            softly.assertThat(review.getRate()).isEqualTo(2.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(2.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
        });
        verifyUpdateOrder(
                staffMembership,
                review,
                new ClubMeetingActor(CLUB_MEMBER_ID, true),
                "운영진 수정",
                2.0
        );
        verifyNoInteractions(bookReviewRepository);
    }

    @Test
    void 한줄평_삭제는_별점을_먼저_차감한_뒤_모임_연관을_해제한다() {
        meeting = meetingWithSumRate(-9.0);
        BookReview review = Mockito.spy(review("삭제 대상", 4.0, CLUB_MEMBER_ID));
        meeting.addBookReview(review);
        meeting.addBookReview(review("남는 한줄평", 6.0, 99L));
        MembershipInfo ownerMembership = allowOwner(review);

        service.deleteBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("삭제 대상");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(review.getMeeting()).isNull();
            softly.assertThat(meeting.getSumRate()).isEqualTo(6.0);
            softly.assertThat(meeting.calculateAverageRate()).isEqualTo(6.0);
        });
        verifyDeleteOrder(ownerMembership, review, new ClubMeetingActor(CLUB_MEMBER_ID, false));
        verifyNoInteractions(bookReviewRepository);
    }

    @Test
    void 운영진은_다른_회원의_한줄평을_삭제할_수_있다() {
        meeting = meetingWithSumRate(0.0);
        BookReview review = Mockito.spy(review("삭제 대상", 4.0, 99L));
        meeting.addBookReview(review);
        MembershipInfo staffMembership = allowStaff(review);

        service.deleteBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("삭제 대상");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(meeting.getSumRate()).isZero();
            softly.assertThat(review.getMeeting()).isNull();
        });
        verifyDeleteOrder(staffMembership, review, new ClubMeetingActor(CLUB_MEMBER_ID, true));
        verifyNoInteractions(bookReviewRepository);
    }

    @Test
    void inactive_회원은_모임과_한줄평을_조회하기_전에_실패하고_상태를_바꾸지_않는다() {
        meeting = meetingWithSumRate(0.0);
        BookReview review = review("기존", 4.0, CLUB_MEMBER_ID);
        meeting.addBookReview(review);
        MembershipInfo inactiveMembership = Mockito.spy(membership(CLUB_MEMBER_ID, false, false));
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID))
                .thenReturn(inactiveMembership);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> service.updateBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID, request("수정", 2.0))
        );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(ClubMeetingErrorStatus.CLUB_MEMBER_INACTIVE);
            softly.assertThat(review.getDescription()).isEqualTo("기존");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
        });
        verifyInactiveValidationOrder(inactiveMembership);
        verifyNoInteractions(clubMeetingQueryService, clubBookReviewQueryService);
        verifyNoInteractions(bookReviewRepository);
    }

    @Test
    void inactive_회원은_한줄평_삭제_시_모임과_한줄평을_조회하기_전에_실패한다() {
        meeting = meetingWithSumRate(0.0);
        BookReview review = review("기존", 4.0, CLUB_MEMBER_ID);
        meeting.addBookReview(review);
        MembershipInfo inactiveMembership = Mockito.spy(membership(CLUB_MEMBER_ID, false, false));
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID))
                .thenReturn(inactiveMembership);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> service.deleteBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID)
        );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(ClubMeetingErrorStatus.CLUB_MEMBER_INACTIVE);
            softly.assertThat(review.getDescription()).isEqualTo("기존");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
        });
        verifyInactiveValidationOrder(inactiveMembership);
        verifyNoInteractions(clubMeetingQueryService, clubBookReviewQueryService);
        verifyNoInteractions(bookReviewRepository);
    }

    @Test
    void 작성자도_운영진도_아닌_활성_회원은_한줄평을_수정할_수_없다() {
        meeting = meetingWithSumRate(0.0);
        BookReview review = Mockito.spy(review("기존", 4.0, CLUB_MEMBER_ID));
        meeting.addBookReview(review);
        MembershipInfo ordinaryMembership = denyOrdinaryMember(review);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> service.updateBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID, request("수정", 2.0))
        );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(ClubMeetingErrorStatus.BOOK_REVIEW_FORBIDDEN);
            softly.assertThat(review.getDescription()).isEqualTo("기존");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
        });
        verifyUpdateOrder(
                ordinaryMembership,
                review,
                new ClubMeetingActor(99L, false),
                "수정",
                2.0
        );
        verifyNoInteractions(bookReviewRepository);
    }

    @Test
    void 작성자도_운영진도_아니면_대상을_조회한_뒤_권한_오류로_실패하고_상태를_유지한다() {
        meeting = meetingWithSumRate(0.0);
        BookReview review = Mockito.spy(review("기존", 4.0, CLUB_MEMBER_ID));
        meeting.addBookReview(review);
        MembershipInfo ordinaryMembership = denyOrdinaryMember(review);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> service.deleteBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID)
        );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(ClubMeetingErrorStatus.BOOK_REVIEW_FORBIDDEN);
            softly.assertThat(review.getDescription()).isEqualTo("기존");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
        });
        verifyDeleteOrder(ordinaryMembership, review, new ClubMeetingActor(99L, false));
        verifyNoInteractions(bookReviewRepository);
    }

    private MembershipInfo allowOwner(BookReview review) {
        MembershipInfo ownerMembership = Mockito.spy(membership(CLUB_MEMBER_ID, true, false));
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID))
                .thenReturn(ownerMembership);
        when(clubMeetingQueryService.validateMeeting(CLUB_ID, MEETING_ID)).thenReturn(meeting);
        when(clubBookReviewQueryService.validateBookReview(REVIEW_ID, MEETING_ID)).thenReturn(review);
        return ownerMembership;
    }

    private MembershipInfo allowStaff(BookReview review) {
        MembershipInfo staffMembership = Mockito.spy(membership(CLUB_MEMBER_ID, true, true));
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID))
                .thenReturn(staffMembership);
        when(clubMeetingQueryService.validateMeeting(CLUB_ID, MEETING_ID)).thenReturn(meeting);
        when(clubBookReviewQueryService.validateBookReview(REVIEW_ID, MEETING_ID)).thenReturn(review);
        return staffMembership;
    }

    private MembershipInfo denyOrdinaryMember(BookReview review) {
        MembershipInfo ordinaryMembership = Mockito.spy(membership(99L, true, false));
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID))
                .thenReturn(ordinaryMembership);
        when(clubMeetingQueryService.validateMeeting(CLUB_ID, MEETING_ID)).thenReturn(meeting);
        when(clubBookReviewQueryService.validateBookReview(REVIEW_ID, MEETING_ID)).thenReturn(review);
        return ordinaryMembership;
    }

    private void verifyUpdateOrder(
            MembershipInfo membership,
            BookReview review,
            ClubMeetingActor actor,
            String description,
            double rate
    ) {
        InOrder inOrder = verifyActorAndLookupOrder(membership, review);
        inOrder.verify(meeting).reviseBookReviewBy(actor, review, description, rate);
    }

    private void verifyDeleteOrder(
            MembershipInfo membership,
            BookReview review,
            ClubMeetingActor actor
    ) {
        InOrder inOrder = verifyActorAndLookupOrder(membership, review);
        inOrder.verify(meeting).removeBookReviewBy(actor, review);
    }

    private InOrder verifyActorAndLookupOrder(MembershipInfo membership, BookReview review) {
        InOrder inOrder = Mockito.inOrder(
                clubManagementAPI,
                membership,
                clubMeetingQueryService,
                clubBookReviewQueryService,
                meeting
        );
        inOrder.verify(clubManagementAPI).validateClub(CLUB_ID);
        inOrder.verify(clubManagementAPI).fetchMembershipInfo(CLUB_ID, MEMBER_ID);
        inOrder.verify(membership).isActive();
        inOrder.verify(membership).getClubMemberId();
        inOrder.verify(membership).isStaff();
        inOrder.verify(clubMeetingQueryService).validateMeeting(CLUB_ID, MEETING_ID);
        inOrder.verify(meeting).getId();
        inOrder.verify(clubBookReviewQueryService).validateBookReview(REVIEW_ID, MEETING_ID);
        return inOrder;
    }

    private void verifyInactiveValidationOrder(MembershipInfo inactiveMembership) {
        InOrder inOrder = Mockito.inOrder(clubManagementAPI, inactiveMembership);
        inOrder.verify(clubManagementAPI).validateClub(CLUB_ID);
        inOrder.verify(clubManagementAPI).fetchMembershipInfo(CLUB_ID, MEMBER_ID);
        inOrder.verify(inactiveMembership).isActive();
        verify(inactiveMembership, Mockito.never()).getClubMemberId();
        verify(inactiveMembership, Mockito.never()).isStaff();
        verify(meeting, Mockito.never()).reviseBookReviewBy(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyDouble()
        );
        verify(meeting, Mockito.never()).removeBookReviewBy(Mockito.any(), Mockito.any());
    }

    private MembershipInfo membership(Long clubMemberId, boolean active, boolean staff) {
        return MembershipInfo.builder()
                .memberId(MEMBER_ID)
                .clubMemberId(clubMemberId)
                .active(active)
                .staff(staff)
                .build();
    }

    private Meeting meetingWithSumRate(double sumRate) {
        return Mockito.spy(Meeting.builder()
                .id(MEETING_ID)
                .clubId(CLUB_ID)
                .bookId("book")
                .sumRate(sumRate)
                .build());
    }

    private BookReview review(String description, double rate, Long clubMemberId) {
        return BookReview.builder()
                .id(clubMemberId)
                .description(description)
                .rate(rate)
                .clubMemberId(clubMemberId)
                .memberId(MEMBER_ID)
                .build();
    }

    private BookReviewCreate request(String description, double rate) {
        BookReviewCreate request = new BookReviewCreate();
        ReflectionTestUtils.setField(request, "description", description);
        ReflectionTestUtils.setField(request, "rate", rate);
        return request;
    }
}
