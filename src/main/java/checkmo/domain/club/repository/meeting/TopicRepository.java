package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByIdAndMeetingId(Long topicId, Long meetingId);
}
