package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByMeetingIdAndTeamNumber(Long meetingId, Integer teamNumber);

    List<Team> findAllByMeetingIdOrderByTeamNumberAsc(Long meetingId);

    @Query("SELECT t.teamNumber FROM Team t WHERE t.meeting.id = :meetingId ORDER BY t.teamNumber ASC")
    List<Integer> findTeamNumberByMeetingId(Long meetingId);
}
