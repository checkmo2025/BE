package checkmo.clubMeeting.internal.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.internal.entity.BookReview;
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
import org.mockito.Mock;
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
        meeting = meetingWithSumRate(4.0);
        BookReview review = review("이전", 4.0, CLUB_MEMBER_ID);
        review.setMeeting(meeting);
        allowOwner(review);

        service.updateBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID, request("수정", 4.0));

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("수정");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
        });
    }

    @Test
    void 별점_수정은_review를_먼저_변경한_뒤_기존_별점을_차감하고_새_별점을_더한다() {
        meeting = meetingWithSumRate(1.0);
        BookReview review = review("이전", 4.0, CLUB_MEMBER_ID);
        review.setMeeting(meeting);
        review("다른 한줄평", 6.0, 99L).setMeeting(meeting);
        allowOwner(review);

        service.updateBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID, request("수정", 2.0));

        assertSoftly(softly -> {
            softly.assertThat(review.getDescription()).isEqualTo("수정");
            softly.assertThat(review.getRate()).isEqualTo(2.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(6.0);
        });
    }

    @Test
    void 한줄평_삭제는_별점을_먼저_차감한_뒤_모임_연관을_해제한다() {
        meeting = meetingWithSumRate(1.0);
        BookReview review = review("삭제 대상", 4.0, CLUB_MEMBER_ID);
        review.setMeeting(meeting);
        review("남는 한줄평", 6.0, 99L).setMeeting(meeting);
        allowOwner(review);

        service.deleteBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID);

        assertSoftly(softly -> {
            softly.assertThat(review.getMeeting()).isNull();
            softly.assertThat(meeting.getSumRate()).isEqualTo(6.0);
            softly.assertThat(meeting.calculateAverageRate()).isEqualTo(6.0);
        });
    }

    @Test
    void inactive_회원은_모임과_한줄평을_조회하기_전에_실패하고_상태를_바꾸지_않는다() {
        meeting = meetingWithSumRate(4.0);
        BookReview review = review("기존", 4.0, CLUB_MEMBER_ID);
        review.setMeeting(meeting);
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID))
                .thenReturn(membership(CLUB_MEMBER_ID, false, false));

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> service.updateBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID, request("수정", 2.0))
        );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(ClubMeetingErrorStatus.CLUB_MEMBER_INACTIVE);
            softly.assertThat(review.getDescription()).isEqualTo("기존");
            softly.assertThat(review.getRate()).isEqualTo(4.0);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
        });
        verifyNoInteractions(clubMeetingQueryService, clubBookReviewQueryService);
    }

    @Test
    void 작성자도_운영진도_아니면_대상을_조회한_뒤_권한_오류로_실패하고_상태를_유지한다() {
        meeting = meetingWithSumRate(4.0);
        BookReview review = review("기존", 4.0, CLUB_MEMBER_ID);
        review.setMeeting(meeting);
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID))
                .thenReturn(membership(99L, true, false));
        when(clubMeetingQueryService.validateMeeting(CLUB_ID, MEETING_ID)).thenReturn(meeting);
        when(clubBookReviewQueryService.validateBookReview(REVIEW_ID, MEETING_ID)).thenReturn(review);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> service.deleteBookReview(CLUB_ID, MEETING_ID, REVIEW_ID, MEMBER_ID)
        );

        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(ClubMeetingErrorStatus.BOOK_REVIEW_FORBIDDEN);
            softly.assertThat(review.getMeeting()).isSameAs(meeting);
            softly.assertThat(meeting.getSumRate()).isEqualTo(4.0);
        });
        verify(clubBookReviewQueryService).validateBookReview(REVIEW_ID, MEETING_ID);
    }

    private void allowOwner(BookReview review) {
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID))
                .thenReturn(membership(CLUB_MEMBER_ID, true, false));
        when(clubMeetingQueryService.validateMeeting(CLUB_ID, MEETING_ID)).thenReturn(meeting);
        when(clubBookReviewQueryService.validateBookReview(REVIEW_ID, MEETING_ID)).thenReturn(review);
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
        return Meeting.builder()
                .id(MEETING_ID)
                .clubId(CLUB_ID)
                .bookId("book")
                .sumRate(sumRate)
                .build();
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
