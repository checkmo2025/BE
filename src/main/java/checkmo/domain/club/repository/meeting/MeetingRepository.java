package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
