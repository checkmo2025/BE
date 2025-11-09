package checkmo.clubMeeting.repository;

import checkmo.clubMeeting.entity.Topic;
import java.util.List;

public interface TopicRepositoryCustom {
    List<Topic> findAllWithClubMemberByCursorOrderByIdDesc(Long meetingId, Long cursorId, Integer size);
}
