package checkmo.realtime.internal.service;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingAPI;
import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final ClubManagementAPI clubManagementAPI;
    private final ClubMeetingAPI clubMeetingAPI;

    public void authorizeTeamAccess(Long clubId, Long meetingId, Long teamId, Long memberId) {
        if (clubMeetingAPI.isNotMeetingBelongsToClub(clubId, meetingId)) {
            throw new RealtimeException(RealtimeErrorStatus.MEETING_NOT_IN_CLUB);
        }

        if (clubMeetingAPI.isNotTeamBelongsToClub(clubId, teamId)) {
            throw new RealtimeException(RealtimeErrorStatus.TEAM_NOT_IN_ClUB);
        }

        Long clubMemberId = clubManagementAPI.fetchActiveClubMemberId(clubId, memberId);
        if (clubMemberId == null) {
            throw new RealtimeException(RealtimeErrorStatus.NOT_ACTIVE_CLUB_MEMBER);
        }

        if (!clubManagementAPI.isStaffClubMember(clubId, memberId)
                && clubMeetingAPI.isNotTeamMember(teamId, clubMemberId)) {
            throw new RealtimeException(RealtimeErrorStatus.NOT_TEAM_MEMBER_OR_STAFF);
        }
    }
}
