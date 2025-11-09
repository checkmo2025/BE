package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.Topic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long>, TopicRepositoryCustom {
    Optional<Topic> findByIdAndMeetingId(Long topicId, Long meetingId);
}
