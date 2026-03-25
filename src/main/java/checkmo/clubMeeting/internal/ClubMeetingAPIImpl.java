package checkmo.clubMeeting.internal;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.clubMeeting.ClubMeetingExternalDTO;
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

import static checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus.TEAM_NOT_FOUND;
import static checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus.TOPIC_NOT_FOUND;

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
    public ClubMeetingExternalDTO.ToggleTopicResult toggleTopic(
            Long meetingId, Long teamId, Long topicId, boolean selected
    ) {
        try {
            boolean result = clubTopicCommandService.toggleTopic(meetingId, teamId, topicId, selected);
            return new ClubMeetingExternalDTO.ToggleTopicResult(result, ClubMeetingExternalDTO.ToggleTopicResult.Failure.NONE);
        } catch (ClubManagementException e) {
            switch (e.getErrorCode()) {
                case TEAM_NOT_FOUND -> {
                    return new ClubMeetingExternalDTO.ToggleTopicResult(selected, ClubMeetingExternalDTO.ToggleTopicResult.Failure.TEAM_NOT_FOUND);
                }
                case TOPIC_NOT_FOUND -> {
                    return new ClubMeetingExternalDTO.ToggleTopicResult(selected, ClubMeetingExternalDTO.ToggleTopicResult.Failure.TOPIC_NOT_FOUND);
                }
                default -> {
                    return new ClubMeetingExternalDTO.ToggleTopicResult(selected, ClubMeetingExternalDTO.ToggleTopicResult.Failure.INTERNAL_ERROR);
                }
            }
        } catch (Exception e) {
            return new ClubMeetingExternalDTO.ToggleTopicResult(selected, ClubMeetingExternalDTO.ToggleTopicResult.Failure.INTERNAL_ERROR);
        }
    }

    @Override
    public boolean isChatDisabled(Long meetingId) {
        return clubMeetingQueryService.retrieveMeeting(meetingId)
                .map(meeting -> {
                    if (meeting.getMeetingTime() == null) {
                        return true;
                    }
                    return LocalDateTime.now().isAfter(meeting.getChatDeadline());
                })
                .orElse(true); // 모임이 존재하지 않는 경우에도 채팅 불가능 처리
    }

}
