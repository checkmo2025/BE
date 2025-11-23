package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMeetingQueryService {

    private final MeetingRepository meetingRepository;

    public List<Meeting> retrieveMeetings(Long clubId, Long cursorId, Integer size) {
        return meetingRepository.findAllByClubIdAndCursorDesc(clubId, cursorId, size);
    }

    public List<Meeting> retrieveMeetings(Long clubId, int year, int month, String memberId) {
        LocalDateTime startDateTime = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusMonths(1);

        return meetingRepository.findAllByClubIdBetweenMeetingTimeAsc(clubId, startDateTime, endDateTime);
    }

    public List<Meeting> retrieveMeetings(Long clubId, Integer generation, Long cursorId, Integer size) {
        return meetingRepository.findAllByClubIdAndGenerationAndCursorDesc(clubId, generation, cursorId, size);
    }
    
    public List<Meeting> retrieveMeetings(Set<Long> meetingIds) {
        return meetingRepository.findAllById(meetingIds);
    }

    public Meeting validateMeeting(Long meetingId) throws ClubMeetingException {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.MEETING_NOT_FOUND));
    }
}
