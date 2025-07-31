package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Topic;

import java.util.List;

public interface TopicRepositoryCustom {
    List<Topic> findTopicsByCursorAsc(Long meetingId, Long cursorId, Integer size);
}
