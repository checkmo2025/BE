package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByMeetingIdAndTeamNumber(Long meetingId, Integer teamNumber);

    List<Team> findTeamsByMeetingIdOrderByTeamNumberAsc(Long meetingId);
}
