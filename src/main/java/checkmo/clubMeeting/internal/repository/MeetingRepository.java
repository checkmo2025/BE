package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.Meeting;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeetingRepository extends JpaRepository<Meeting, Long>, MeetingRepositoryCustom {
    @Query("SELECT m FROM Meeting m " +
            "WHERE m.clubId = :clubId " +
            "AND m.meetingTime >= :startDate " +
            "AND m.meetingTime < :endDate " +
            "ORDER BY m.meetingTime ASC")
        // DATE_FORMAT이나 TO_CHAR은 모든 ROW에 대해 해당 함수를 적용하기 때문에 성능 상 범위 탐색으로 찾기
    List<Meeting> findAllByClubIdBetweenMeetingTimeAsc(Long clubId, LocalDateTime startDate, LocalDateTime endDate);
}
