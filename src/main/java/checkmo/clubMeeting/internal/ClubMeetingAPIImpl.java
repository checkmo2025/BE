package checkmo.clubMeeting.internal;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
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
    public boolean isMeetingInClub(Long clubId, Long meetingId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        return meeting.getClubId().equals(clubId);
    }

}
