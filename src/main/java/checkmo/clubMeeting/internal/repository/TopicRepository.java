package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.Topic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByIdAndMeetingId(Long topicId, Long meetingId);

    @Query("SELECT t FROM Topic t "
            + "WHERE t.meeting.id = :meetingId "
            + "AND (:cursorId IS NULL OR t.id > :cursorId) "
            + "ORDER BY t.id ASC")
    List<Topic> findByMeetingIdWithCursor(Long meetingId, Long cursorId, Pageable pageable);
}
