package checkmo.clubMeeting.internal;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.service.command.ClubTopicCommandService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingTeamQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMeetingAPIImpl implements ClubMeetingAPI {

    private final BookAPI bookAPI; // TODO: Meeting 내부에 Book 스냅샷 저장 예정이라 나중에는 Meeting에서 직접 조회해서 필요없을 예정
    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubMeetingTeamQueryService clubMeetingTeamQueryService;
    private final ClubTopicCommandService clubTopicCommandService;

    @Override
    public DetailInfo fetchMeetingDetailInfo(Long meetingId) {
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        BookExternalDTO.BasicInfo bookBasicInfoForShare = bookAPI.fetchBookBasicInfo(meeting.getBookId());
        return ClubMeetingConverter.toMeetingInfoExternalDTO(meeting, bookBasicInfoForShare);
    }

    @Override
    public boolean isNotMeetingBelongsToClub(Long clubId, Long meetingId) {
        try {
            Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
            return !meeting.getClubId().equals(clubId);
        } catch (ClubMeetingException e) {
            return true;
        }
    }

    @Override
    public boolean isNotTeamBelongsToClub(Long clubId, Long teamId) {
        try {
            return !clubMeetingTeamQueryService.isBelongsToClub(clubId, teamId);
        } catch (ClubMeetingException e) {
            return true;
        }
    }

    @Override
    public boolean isNotTeamMember(Long teamId, Long clubMemberId) {
        return !clubMeetingTeamQueryService.isTeamMember(teamId, clubMemberId);
    }

    @Override
    @Transactional
    public boolean toggleTopic(
            Long clubId, Long meetingId, Long teamId,
            Long topicId, boolean selected, String memberId
    ) {
        return clubTopicCommandService.toggleTopic(clubId, meetingId, teamId, topicId, selected, memberId);
    }

    @Override
    public boolean isChatDisabled(Long meetingId) {
        try {
            Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
            if (meeting.getMeetingTime() == null) {
                return true; // 모임 시간이 설정되지 않은 경우 채팅 불가능
            }
            LocalDateTime deadline = meeting.getChatDeadline();
            return LocalDateTime.now().isAfter(deadline); // 모임 날짜로부터 3일이 지났으면 채팅 불가능
        } catch (ClubMeetingException e) {
            // 모임이 존재하지 않는 경우에도 채팅 불가능 처리
            return true;
        }
    }

}
