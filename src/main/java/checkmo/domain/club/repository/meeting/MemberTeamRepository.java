package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.MemberTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTeamRepository extends JpaRepository<MemberTeam, Long> {
}
