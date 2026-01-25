package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.TeamTopic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamTopicRepository extends JpaRepository<TeamTopic, Long> {
    Optional<TeamTopic> findByTeamIdAndTopicId(Long teamId, Long topicId);

    @Query("SELECT tt.topic.id "
            + "FROM TeamTopic tt "
            + "WHERE tt.team.id = :teamId "
            + "AND tt.topic.id IN :topicIds")
    List<Long> findTopicIdsByTeamIdAndTopicIds(Long teamId, List<Long> topicIds);
}
