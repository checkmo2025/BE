package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.Meeting;
import java.util.List;

public interface MeetingRepositoryCustom {
    List<Meeting> findAllByClubIdAndCursorDesc(Long clubId, Long cursorId, Integer size);

    List<Meeting> findAllByClubIdAndGenerationAndCursorDesc(
            Long clubId,
            Integer generation,
            Long cursorId,
            Integer size
    );
}
