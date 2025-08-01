package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Meeting;

import java.util.List;

public interface MeetingRepositoryCustom {
    List<Meeting> findMeetingsByClubIdAndCursorDesc(Long clubId, Long cursorId, Integer size);
}
