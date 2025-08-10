package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.TeamTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamTopicRepository extends JpaRepository<TeamTopic, Long> {
    @Query("SELECT tt FROM TeamTopic tt " +
            "JOIN FETCH tt.team t " +
            "WHERE tt.topic.id IN :topicIds " +
            "ORDER BY t.teamNumber ASC")
    List<TeamTopic> findTeamTopicsWithTeamByTopicIds(List<Long> topicIds);

    @Query("SELECT tt " +
            "FROM TeamTopic tt " +
            "JOIN FETCH tt.topic t " +
            "JOIN FETCH t.clubMember cm " +
            "WHERE tt.teamId = :teamId")
    List<TeamTopic> findTeamTopicsWithTopicAndClubMemberByTeamId(Long teamId);

    Optional<TeamTopic> findByTeamIdAndTopicId(Long teamId, Long topicId);
}
