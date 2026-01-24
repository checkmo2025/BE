package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.Meeting;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long>, MeetingRepositoryCustom {
    Optional<Meeting> findTop1ByClubIdAndMeetingTimeGreaterThanEqualOrderByMeetingTimeAscIdAsc(
            Long clubId, LocalDateTime now);

    Optional<Meeting> findByClubIdAndId(Long clubId, Long meetingId);
}
