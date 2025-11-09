package checkmo.clubMeeting.repository;

import checkmo.clubMeeting.entity.TeamTopic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamTopicRepository extends JpaRepository<TeamTopic, Long> {
    @Query("SELECT tt FROM TeamTopic tt " +
            "JOIN FETCH tt.team t " +
            "WHERE tt.topicId IN :topicIds " +
            "ORDER BY t.teamNumber ASC")
    List<TeamTopic> findAllWithTeamByTopicIds(List<Long> topicIds);

    @Query("SELECT tt " +
            "FROM TeamTopic tt " +
            "JOIN FETCH tt.topic t " +
            "JOIN FETCH t.clubMember cm " +
            "WHERE tt.teamId = :teamId " +
            "ORDER BY t.id DESC ")
    List<TeamTopic> findAllWithTopicAndClubMemberByTeamIdOrderByDesc(Long teamId, Pageable pageable);

    Optional<TeamTopic> findByTeamIdAndTopicId(Long teamId, Long topicId);
}
