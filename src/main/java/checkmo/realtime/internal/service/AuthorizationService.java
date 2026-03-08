package checkmo.realtime.internal.service;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final ClubManagementAPI clubManagementAPI;
    private final ClubMeetingAPI clubMeetingAPI;

    public void authorizeTeamAccess(Long clubId, Long meetingId, Long teamId, String memberId) {
        if (clubMeetingAPI.isNotMeetingBelongsToClub(clubId, meetingId)) {
            throw new RealtimeException(RealtimeErrorStatus.MEETING_NOT_IN_CLUB);
        }

        if (clubMeetingAPI.isNotTeamBelongsToClub(clubId, teamId)) {
            throw new RealtimeException(RealtimeErrorStatus.TEAM_NOT_IN_ClUB);
        }

        final Long clubMemberId;
        try {
            clubMemberId = clubManagementAPI.fetchActiveClubMemberId(clubId, memberId);
        } catch (Exception e) {
            throw new RealtimeException(RealtimeErrorStatus.NOT_ACTIVE_CLUB_MEMBER);
        }

        if (!isStaff(clubId, memberId) && clubMeetingAPI.isNotTeamMember(teamId, clubMemberId)) {
            throw new RealtimeException(RealtimeErrorStatus.NOT_TEAM_MEMBER_OR_STAFF);
        }
    }

    private boolean isStaff(Long clubId, String memberId) {
        try {
            clubManagementAPI.validateStaffClubMember(clubId, memberId);
            return true;
        } catch (GeneralException ignore) {
            return false;
        }
    }
}
