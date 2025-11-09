package checkmo.clubMeeting.repository;

import checkmo.clubMeeting.entity.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByMeetingIdAndTeamNumber(Long meetingId, Integer teamNumber);

    List<Team> findAllByMeetingIdOrderByTeamNumberAsc(Long meetingId);
}
