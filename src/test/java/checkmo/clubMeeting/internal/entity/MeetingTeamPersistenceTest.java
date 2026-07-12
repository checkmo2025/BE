package checkmo.clubMeeting.internal.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import checkmo.book.internal.scheduler.BookRecommendationScheduler;
import checkmo.bookStory.internal.scheduler.BookStoryViewScheduler;
import checkmo.clubMeeting.internal.repository.ClubMemberTeamRepository;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import checkmo.member.internal.scheduler.MemberCleanupScheduler;
import checkmo.support.SpringTest;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringTest
@Transactional
class MeetingTeamPersistenceTest {

    @MockitoBean
    BookRecommendationScheduler bookRecommendationScheduler;

    @MockitoBean
    BookStoryViewScheduler bookStoryViewScheduler;

    @MockitoBean
    MemberCleanupScheduler memberCleanupScheduler;

    private final EntityManager entityManager;
    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;
    private final ClubMemberTeamRepository clubMemberTeamRepository;
    private final TopicRepository topicRepository;
    private final TeamTopicRepository teamTopicRepository;

    MeetingTeamPersistenceTest(
            EntityManager entityManager,
            MeetingRepository meetingRepository,
            TeamRepository teamRepository,
            ClubMemberTeamRepository clubMemberTeamRepository,
            TopicRepository topicRepository,
            TeamTopicRepository teamTopicRepository
    ) {
        this.entityManager = entityManager;
        this.meetingRepository = meetingRepository;
        this.teamRepository = teamRepository;
        this.clubMemberTeamRepository = clubMemberTeamRepository;
        this.topicRepository = topicRepository;
        this.teamTopicRepository = teamTopicRepository;
    }

    @Test
    void 팀_재구성은_유지_팀의_ID를_보존하고_삭제_팀의_자식행을_제거하며_신규_팀을_저장한다() {
        Meeting meeting = meetingRepository.save(Meeting.builder()
                .title("meeting")
                .clubId(1L)
                .bookId("book")
                .build());
        Team removed = addTeam(meeting, 1, 10L);
        Team retained = addTeam(meeting, 2, 20L);
        meetingRepository.flush();

        Topic topic = Topic.builder()
                .description("topic")
                .clubMemberId(1L)
                .memberId(1L)
                .build();
        topic.setMeeting(meeting);
        topicRepository.save(topic);
        TeamTopic removedTeamTopic = TeamTopic.builder().build();
        removedTeamTopic.setTeam(removed);
        removedTeamTopic.setTopic(topic);
        entityManager.flush();

        Long meetingId = meeting.getId();
        Long removedTeamId = removed.getId();
        Long removedMemberRowId = removed.getClubMemberTeams().get(0).getId();
        Long removedTeamTopicId = removedTeamTopic.getId();
        Long retainedTeamId = retained.getId();
        entityManager.clear();

        Meeting reloadedMeeting = meetingRepository.findById(meetingId).orElseThrow();
        List<Team> existingTeams = teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meetingId);
        Map<Integer, List<Long>> requestedMembersByTeamNumber = new LinkedHashMap<>();
        requestedMembersByTeamNumber.put(2, List.of(21L, 22L));
        requestedMembersByTeamNumber.put(3, List.of(31L));

        reloadedMeeting.reconfigureTeams(existingTeams, requestedMembersByTeamNumber);
        meetingRepository.save(reloadedMeeting);
        entityManager.flush();
        entityManager.clear();

        Team persistedRetained = teamRepository.findByMeetingIdAndTeamNumber(meetingId, 2).orElseThrow();
        Team persistedCreated = teamRepository.findByMeetingIdAndTeamNumber(meetingId, 3).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(persistedRetained.getId()).isEqualTo(retainedTeamId);
            softly.assertThat(teamRepository.findById(removedTeamId)).isEmpty();
            softly.assertThat(clubMemberTeamRepository.findById(removedMemberRowId)).isEmpty();
            softly.assertThat(teamTopicRepository.findById(removedTeamTopicId)).isEmpty();
            softly.assertThat(persistedCreated.getId()).isNotNull().isNotEqualTo(retainedTeamId);
            softly.assertThat(clubMemberTeamRepository.findAllByTeamIds(List.of(persistedRetained.getId())))
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(21L, 22L);
            softly.assertThat(clubMemberTeamRepository.findAllByTeamIds(List.of(persistedCreated.getId())))
                    .extracting(ClubMemberTeam::getClubMemberId)
                    .containsExactly(31L);
        });
        assertThat(teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meetingId))
                .extracting(Team::getTeamNumber)
                .containsExactly(2, 3);
    }

    private Team addTeam(Meeting meeting, int teamNumber, Long... clubMemberIds) {
        Team team = Team.builder().teamNumber(teamNumber).build();
        meeting.addTeam(team);
        team.replaceMembers(List.of(clubMemberIds));
        return team;
    }
}
