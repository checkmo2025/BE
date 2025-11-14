package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.repository.MeetingRepository;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMeetingQueryServiceImpl implements ClubMeetingQueryService {
    private final MeetingRepository meetingRepository;

    @Override
    public List<Meeting> findMeetingsByClubAndCursor(Long clubId, Long cursorId, Integer size) {
        return meetingRepository.findAllByClubIdAndCursorDesc(clubId, cursorId, size);
    }

    @Override
    public List<Meeting> getBookShelfList(Long clubId, Integer generation, Long cursorId, Integer size) {
        return meetingRepository.findAllByClubIdAndGenerationAndCursorDesc(clubId, generation, cursorId, size);
    }

    @Override
    public List<Meeting> getClubMeetingByYearAndMonth(Long clubId, int year, int month, String memberId) {
        LocalDateTime startDateTime = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusMonths(1); //12월의 경우 다음 해 1월로 넘어감

        return meetingRepository.findAllByClubIdBetweenMeetingTimeAsc(clubId, startDateTime, endDateTime);
    }

    @Override
    public Meeting validateMeeting(Long meetingId) throws GeneralException {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEETING_NOT_FOUND));
    }

}
