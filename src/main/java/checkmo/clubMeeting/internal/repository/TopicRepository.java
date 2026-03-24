package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.Topic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findAllByMeetingIdOrderByIdDesc(Long meetingId);

    Optional<Topic> findByIdAndMeetingId(Long topicId, Long meetingId);

    @Query("SELECT t FROM Topic t "
            + "WHERE t.meeting.id = :meetingId "
            + "AND (:cursorId IS NULL OR t.id < :cursorId) "
            + "ORDER BY t.id DESC, t.createdAt DESC ")
    List<Topic> findByMeetingIdWithCursor(Long meetingId, Long cursorId, Pageable pageable);
}
