package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Meeting;

import java.util.List;

public interface MeetingRepositoryCustom {
    List<Meeting> findAllByClubIdAndCursorDesc(Long clubId, Long cursorId, Integer size);

    List<Meeting> findAllByClubIdAndGenerationAndCursorDesc(Long clubId, Integer generation, Long cursorId, Integer size);
}
