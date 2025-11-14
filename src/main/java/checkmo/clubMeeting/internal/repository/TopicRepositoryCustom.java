package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.Topic;
import java.util.List;

public interface TopicRepositoryCustom {
    List<Topic> findAllByCursorOrderByIdDesc(Long meetingId, Long cursorId, Integer size);
}
