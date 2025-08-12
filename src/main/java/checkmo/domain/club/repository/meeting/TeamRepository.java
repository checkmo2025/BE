package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findTeamsByMeetingId(Long meetingId);
}
