package checkmo.club.repository.meeting;

import checkmo.club.entity.meeting.Topic;

import java.util.List;

public interface TopicRepositoryCustom {
    List<Topic> findAllWithClubMemberByCursorOrderByIdDesc(Long meetingId, Long cursorId, Integer size);
}
