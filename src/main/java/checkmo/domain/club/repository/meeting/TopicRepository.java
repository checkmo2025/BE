package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
