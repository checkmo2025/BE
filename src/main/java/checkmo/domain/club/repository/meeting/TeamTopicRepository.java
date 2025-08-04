package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.TeamTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamTopicRepository extends JpaRepository<TeamTopic, Long> {
    @Query("SELECT tt FROM TeamTopic tt " +
            "JOIN FETCH tt.team t " +
            "WHERE tt.topic.id IN :topicIds")
    List<TeamTopic> findTeamTopicsWithTeamByTopicIds(List<Long> topicIds);
}
