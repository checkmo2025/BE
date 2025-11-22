package checkmo.clubMeeting.internal;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.book.BookExternalDTO.BasicInfo;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMeetingAPIImpl implements ClubMeetingAPI {

    private final BookAPI bookAPI; // TODO: Meeting 내부에 Book 스냅샷 저장 예정이라 나중에는 Meeting에서 직접 조회해서 필요없을 예정
    private final ClubMeetingQueryService clubMeetingQueryService;

    @Override
    public DetailInfo fetchMeetingDetailInfo(Long meetingId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        BookExternalDTO.BasicInfo bookBasicInfoForShare = bookAPI.fetchBookBasicInfo(meeting.getBookId());
        return ClubMeetingConverter.toMeetingInfoExternalDTO(meeting, bookBasicInfoForShare);
    }

    @Override
    public Map<Long, DetailInfo> fetchMeetingDetailInfoByMeetingIds(Set<Long> meetingIds) {
        if (meetingIds == null) {
            return Map.of();
        }

        List<Meeting> meetings = clubMeetingQueryService.getMeetingsByIds(meetingIds);
        if (meetings.size() != meetingIds.size()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.MEETING_NOT_FOUND);
        }

        List<String> bookIds = extractBookIds(meetings);

        Map<String, BasicInfo> bookBasicInfo = bookAPI.fetchBookBasicInfoByBookIds(bookIds);

        return toMeetingInfoMap(meetings, bookBasicInfo);
    }

    private Map<Long, DetailInfo> toMeetingInfoMap(List<Meeting> meetings, Map<String, BasicInfo> bookBasicInfo) {
        return meetings.stream()
                .collect(Collectors.toMap(
                        Meeting::getId,
                        meeting -> ClubMeetingConverter.toMeetingInfoExternalDTO(
                                meeting,
                                bookBasicInfo.get(meeting.getBookId())
                        )
                ));
    }

    private List<String> extractBookIds(List<Meeting> meetings) {
        return meetings.stream()
                .map(Meeting::getBookId)
                .distinct()
                .toList();
    }
}
