package checkmo.clubMeeting.internal.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import checkmo.book.BookAPI;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamManage;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO.TeamMember;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClubMeetingCommandServiceTest {

    private static final Long CLUB_ID = 1L;
    private static final Long MEETING_ID = 2L;
    private static final Long ACTOR_ID = 3L;

    @Mock
    private BookAPI bookAPI;
    @Mock
    private ClubManagementAPI clubManagementAPI;
    @Mock
    private ClubMeetingQueryService clubMeetingQueryService;
    @Mock
    private MeetingRepository meetingRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    private ClubMeetingCommandService service;

    private Meeting meeting;

    @BeforeEach
    void setUp() {
        meeting = Meeting.builder().id(MEETING_ID).clubId(CLUB_ID).bookId("book").build();
        when(clubMeetingQueryService.validateMeeting(CLUB_ID, MEETING_ID)).thenReturn(meeting);
    }

    @Test
    void 팀원_목록이_null이면_모든_팀을_제거하고_모임을_한_번_저장한다() {
        Team first = team(1, 10L);
        Team second = team(2, 20L);

        service.manageTeam(CLUB_ID, MEETING_ID, ACTOR_ID, request(null));

        assertSoftly(softly -> {
            softly.assertThat(meeting.getTeams()).isEmpty();
            softly.assertThat(first.getMeeting()).isNull();
            softly.assertThat(second.getMeeting()).isNull();
        });
        verify(meetingRepository).save(meeting);
        verifyNoInteractions(teamRepository);
    }

    @Test
    void 팀원_목록이_비어있으면_모든_팀을_제거하고_모임을_한_번_저장한다() {
        Team first = team(1, 10L);
        Team second = team(2, 20L);

        service.manageTeam(CLUB_ID, MEETING_ID, ACTOR_ID, request(List.of()));

        assertSoftly(softly -> {
            softly.assertThat(meeting.getTeams()).isEmpty();
            softly.assertThat(first.getMeeting()).isNull();
            softly.assertThat(second.getMeeting()).isNull();
        });
        verify(meetingRepository).save(meeting);
        verifyNoInteractions(teamRepository);
    }

    @Test
    void 요청에_남은_팀번호는_기존_Team_인스턴스를_재사용한다() {
        Team retained = team(1, 10L);
        when(teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(MEETING_ID)).thenReturn(List.of(retained));

        service.manageTeam(CLUB_ID, MEETING_ID, ACTOR_ID, request(List.of(member(1, 11L))));

        assertThat(meeting.getTeams()).singleElement().isSameAs(retained);
    }

    @Test
    void 요청한_새_팀은_추가하고_생략한_기존_팀은_제거한다() {
        Team removed = team(1, 10L);
        Team retained = team(2, 20L);
        when(teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(MEETING_ID))
                .thenReturn(List.of(removed, retained));

        service.manageTeam(CLUB_ID, MEETING_ID, ACTOR_ID,
                request(List.of(member(2, 21L), member(3, 31L))));

        Team created = findTeam(3);
        assertSoftly(softly -> {
            softly.assertThat(meeting.getTeams()).containsExactlyInAnyOrder(retained, created);
            softly.assertThat(findTeam(2)).isSameAs(retained);
            softly.assertThat(created.getMeeting()).isSameAs(meeting);
            softly.assertThat(removed.getMeeting()).isNull();
        });
    }

    @Test
    void 기존_팀원은_요청한_distinct_ID의_자식으로_교체하고_역방향_연관을_연결한다() {
        Team retained = team(1, 10L, 20L);
        when(teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(MEETING_ID)).thenReturn(List.of(retained));

        service.manageTeam(CLUB_ID, MEETING_ID, ACTOR_ID,
                request(List.of(member(1, 30L, 30L, null, 40L))));

        assertThat(retained.getClubMemberTeams())
                .extracting(ClubMemberTeam::getClubMemberId)
                .containsExactly(30L, 40L);
        assertThat(retained.getClubMemberTeams()).allSatisfy(child -> assertThat(child.getTeam()).isSameAs(retained));
    }

    @Test
    void active_회원_배치검증은_정확한_요청_ID_set으로_aggregate_변경_전에_실행한다() {
        Team retained = team(1, 10L);
        Team omitted = team(2, 20L);
        doAnswer(invocation -> {
            assertThat(invocation.<Set<Long>>getArgument(1)).containsExactlyInAnyOrder(30L, 40L);
            assertThat(meeting.getTeams()).containsExactly(retained, omitted);
            assertThat(retained.getClubMemberTeams())
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(10L);
            return null;
        }).when(clubManagementAPI).validateActiveClubMembers(CLUB_ID, Set.of(30L, 40L));
        when(teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(MEETING_ID))
                .thenReturn(List.of(retained, omitted));

        service.manageTeam(CLUB_ID, MEETING_ID, ACTOR_ID,
                request(List.of(member(1, 30L, 30L), member(3, 40L))));

        verify(clubManagementAPI).validateActiveClubMembers(CLUB_ID, Set.of(30L, 40L));
    }

    @ParameterizedTest
    @EnumSource(value = ClubManagementErrorStatus.class, names = {
            "CLUB_MEMBER_NOT_FOUND", "CLUB_MEMBER_IS_NOT_ACTIVE"
    })
    void 요청_회원_검증이_실패하면_aggregate를_변경하지_않고_저장하지_않는다(
            ClubManagementErrorStatus errorStatus
    ) {
        Team retained = team(1, 10L);
        Team omitted = team(2, 20L);
        ClubManagementException failure = new ClubManagementException(errorStatus);
        doThrow(failure).when(clubManagementAPI).validateActiveClubMembers(CLUB_ID, Set.of(30L, 40L));

        assertThatThrownBy(() -> service.manageTeam(CLUB_ID, MEETING_ID, ACTOR_ID,
                request(List.of(member(1, 30L), member(3, 40L)))))
                .isSameAs(failure);

        assertSoftly(softly -> {
            softly.assertThat(meeting.getTeams()).containsExactly(retained, omitted);
            softly.assertThat(retained.getClubMemberTeams())
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(10L);
            softly.assertThat(omitted.getClubMemberTeams())
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(20L);
        });
        verify(meetingRepository, never()).save(meeting);
        verifyNoInteractions(teamRepository);
    }

    private Team team(int teamNumber, Long... clubMemberIds) {
        Team team = Team.builder().teamNumber(teamNumber).build();
        meeting.addTeam(team);
        team.replaceMembers(Arrays.asList(clubMemberIds));
        return team;
    }

    private Team findTeam(int teamNumber) {
        return meeting.getTeams().stream()
                .filter(team -> team.getTeamNumber() == teamNumber)
                .findFirst()
                .orElseThrow();
    }

    private TeamManage request(List<TeamMember> members) {
        TeamManage request = new TeamManage();
        ReflectionTestUtils.setField(request, "teamMemberList", members);
        return request;
    }

    private TeamMember member(int teamNumber, Long... clubMemberIds) {
        TeamMember member = new TeamMember();
        ReflectionTestUtils.setField(member, "teamNumber", teamNumber);
        ReflectionTestUtils.setField(member, "clubMemberIds", Arrays.asList(clubMemberIds));
        return member;
    }
}
