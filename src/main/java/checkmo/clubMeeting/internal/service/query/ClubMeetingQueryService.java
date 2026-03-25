package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMeetingQueryService {

    private final MeetingRepository meetingRepository;

    public Optional<Meeting> retrieveMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId);
    }

    public List<Meeting> retrieveMeetings(Long clubId, Long cursorId, Integer size) {
        return meetingRepository.findAllByClubIdAndCursorDesc(clubId, cursorId, size);
    }

    public Meeting retrieveNextFutureMeeting(Long clubId, LocalDateTime now) {
        return meetingRepository.findTop1ByClubIdAndMeetingTimeGreaterThanEqualOrderByMeetingTimeAscIdAsc(clubId, now)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.NEXT_MEETING_NOT_FOUND));
    }

    public Meeting validateMeeting(Long meetingId) throws ClubMeetingException {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.MEETING_NOT_FOUND));
    }

    public Meeting validateMeeting(Long clubId, Long meetingId) throws ClubMeetingException {
        return meetingRepository.findByClubIdAndId(clubId, meetingId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.MEETING_NOT_FOUND));
    }
}
