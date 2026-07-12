package checkmo.clubMeeting.internal.service.command;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingTeamQueryService;
import checkmo.clubMeeting.internal.service.query.ClubTopicQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.TopicCreate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClubTopicCommandServiceTest {

    private static final Long CLUB_ID = 1L;
    private static final Long MEETING_ID = 2L;
    private static final Long TOPIC_ID = 3L;
    private static final Long MEMBER_ID = 4L;
    private static final Long AUTHOR_CLUB_MEMBER_ID = 5L;
    private static final Long ANOTHER_CLUB_MEMBER_ID = 6L;
    private static final String ORIGINAL_DESCRIPTION = "기존 발제";
    private static final String UPDATED_DESCRIPTION = "수정된 발제";

    @Mock
    private ClubManagementAPI clubManagementAPI;
    @Mock
    private ClubMeetingQueryService clubMeetingQueryService;
    @Mock
    private ClubTopicQueryService clubTopicQueryService;
    @Mock
    private ClubMeetingTeamQueryService clubMeetingTeamQueryService;
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private TeamTopicRepository teamTopicRepository;
    @InjectMocks
    private ClubTopicCommandService service;

    private Meeting meeting;

    @BeforeEach
    void setUp() {
        meeting = Meeting.builder()
                .id(MEETING_ID)
                .clubId(CLUB_ID)
                .bookId("book")
                .build();
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void 작성자는_발제를_수정하거나_삭제할_수_있다(Operation operation) {
        MembershipInfo membership = spy(membership(AUTHOR_CLUB_MEMBER_ID, true, false));
        Topic topic = topic(AUTHOR_CLUB_MEMBER_ID);
        allow(membership, topic);

        execute(operation);

        verifyAuthorizedOrder(membership, topic, AUTHOR_CLUB_MEMBER_ID, false, operation);
        assertSuccessState(operation, topic);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void 운영진은_다른_작성자의_발제를_수정하거나_삭제할_수_있다(Operation operation) {
        MembershipInfo membership = spy(membership(ANOTHER_CLUB_MEMBER_ID, true, true));
        Topic topic = topic(AUTHOR_CLUB_MEMBER_ID);
        allow(membership, topic);

        execute(operation);

        verifyAuthorizedOrder(membership, topic, ANOTHER_CLUB_MEMBER_ID, true, operation);
        assertSuccessState(operation, topic);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void active_일반_회원은_대상_조회_후_권한_오류로_실패하고_발제를_변경하지_않는다(Operation operation) {
        MembershipInfo membership = spy(membership(ANOTHER_CLUB_MEMBER_ID, true, false));
        Topic topic = topic(AUTHOR_CLUB_MEMBER_ID);
        allow(membership, topic);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> execute(operation)
        );

        verifyForbiddenOrder(membership, topic, ANOTHER_CLUB_MEMBER_ID);
        verifyNoMutation(operation, topic);
        assertFailureState(thrown, ClubMeetingErrorStatus.TOPIC_FORBIDDEN, topic);
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void inactive_회원은_모임과_발제_조회_전에_실패하고_발제를_변경하지_않는다(Operation operation) {
        MembershipInfo membership = spy(membership(ANOTHER_CLUB_MEMBER_ID, false, false));
        Topic topic = topic(AUTHOR_CLUB_MEMBER_ID);
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID)).thenReturn(membership);

        ClubMeetingException thrown = catchThrowableOfType(
                ClubMeetingException.class,
                () -> execute(operation)
        );

        InOrder order = inOrder(clubManagementAPI, membership);
        order.verify(clubManagementAPI).validateClub(CLUB_ID);
        order.verify(clubManagementAPI).fetchMembershipInfo(CLUB_ID, MEMBER_ID);
        order.verify(membership).isActive();
        verify(membership, never()).getClubMemberId();
        verify(membership, never()).isStaff();
        verifyNoInteractions(clubMeetingQueryService, clubTopicQueryService);
        verifyNoMutation(operation, topic);
        assertFailureState(thrown, ClubMeetingErrorStatus.CLUB_MEMBER_INACTIVE, topic);
    }

    private void allow(MembershipInfo membership, Topic topic) {
        when(clubManagementAPI.fetchMembershipInfo(CLUB_ID, MEMBER_ID)).thenReturn(membership);
        when(clubMeetingQueryService.validateMeeting(CLUB_ID, MEETING_ID)).thenReturn(meeting);
        when(clubTopicQueryService.validateTopic(TOPIC_ID, MEETING_ID)).thenReturn(topic);
    }

    private void verifyAuthorizedOrder(
            MembershipInfo membership,
            Topic topic,
            Long actorClubMemberId,
            boolean staffAuthorization,
            Operation operation
    ) {
        InOrder order = verifyLookupAndOwnershipOrder(membership, topic, actorClubMemberId);
        if (staffAuthorization) {
            order.verify(membership).isStaff();
        } else {
            verify(membership, never()).isStaff();
        }
        verifyMutation(order, operation, topic);
    }

    private void verifyForbiddenOrder(MembershipInfo membership, Topic topic, Long actorClubMemberId) {
        InOrder order = verifyLookupAndOwnershipOrder(membership, topic, actorClubMemberId);
        order.verify(membership).isStaff();
    }

    private InOrder verifyLookupAndOwnershipOrder(
            MembershipInfo membership,
            Topic topic,
            Long actorClubMemberId
    ) {
        InOrder order = inOrder(
                clubManagementAPI,
                membership,
                clubMeetingQueryService,
                clubTopicQueryService,
                topic
        );
        order.verify(clubManagementAPI).validateClub(CLUB_ID);
        order.verify(clubManagementAPI).fetchMembershipInfo(CLUB_ID, MEMBER_ID);
        order.verify(membership).isActive();
        order.verify(clubMeetingQueryService).validateMeeting(CLUB_ID, MEETING_ID);
        order.verify(clubTopicQueryService).validateTopic(TOPIC_ID, MEETING_ID);
        order.verify(membership).getClubMemberId();
        order.verify(topic).isOwnedBy(actorClubMemberId);
        return order;
    }

    private void verifyMutation(InOrder order, Operation operation, Topic topic) {
        if (operation == Operation.UPDATE) {
            order.verify(topic).updateTopic(UPDATED_DESCRIPTION);
            return;
        }
        order.verify(topic).removeMeeting();
    }

    private void verifyNoMutation(Operation operation, Topic topic) {
        if (operation == Operation.UPDATE) {
            verify(topic, never()).updateTopic(UPDATED_DESCRIPTION);
            return;
        }
        verify(topic, never()).removeMeeting();
    }

    private void assertSuccessState(Operation operation, Topic topic) {
        assertSoftly(softly -> {
            if (operation == Operation.UPDATE) {
                softly.assertThat(topic.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
                softly.assertThat(topic.getMeeting()).isSameAs(meeting);
                return;
            }
            softly.assertThat(topic.getDescription()).isEqualTo(ORIGINAL_DESCRIPTION);
            softly.assertThat(topic.getMeeting()).isNull();
        });
    }

    private void assertFailureState(
            ClubMeetingException thrown,
            ClubMeetingErrorStatus expectedError,
            Topic topic
    ) {
        assertSoftly(softly -> {
            softly.assertThat(thrown.getErrorCode()).isEqualTo(expectedError);
            softly.assertThat(topic.getDescription()).isEqualTo(ORIGINAL_DESCRIPTION);
            softly.assertThat(topic.getMeeting()).isSameAs(meeting);
        });
    }

    private void execute(Operation operation) {
        if (operation == Operation.UPDATE) {
            service.updateTopic(CLUB_ID, MEETING_ID, TOPIC_ID, MEMBER_ID, request(UPDATED_DESCRIPTION));
            return;
        }
        service.deleteTopic(CLUB_ID, MEETING_ID, TOPIC_ID, MEMBER_ID);
    }

    private Topic topic(Long clubMemberId) {
        Topic topic = spy(Topic.builder()
                .id(TOPIC_ID)
                .description(ORIGINAL_DESCRIPTION)
                .clubMemberId(clubMemberId)
                .memberId(MEMBER_ID)
                .build());
        topic.setMeeting(meeting);
        return topic;
    }

    private MembershipInfo membership(Long clubMemberId, boolean active, boolean staff) {
        return MembershipInfo.builder()
                .memberId(MEMBER_ID)
                .clubMemberId(clubMemberId)
                .active(active)
                .staff(staff)
                .build();
    }

    private TopicCreate request(String description) {
        TopicCreate request = new TopicCreate();
        ReflectionTestUtils.setField(request, "description", description);
        return request;
    }

    private enum Operation {
        UPDATE,
        DELETE
    }
}
