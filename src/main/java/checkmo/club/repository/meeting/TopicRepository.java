package checkmo.club.repository.meeting;

import checkmo.club.entity.meeting.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long>, TopicRepositoryCustom {
    Optional<Topic> findByIdAndMeetingId(Long topicId, Long meetingId);
}
