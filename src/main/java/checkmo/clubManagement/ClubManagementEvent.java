package checkmo.clubManagement;

import lombok.Builder;

public class ClubManagementEvent {

    @Builder
    public record JoinClubEvent(Long eventId, Long memberId, Long clubId, String clubName) {
    }

    @Builder
    public record LeaveClubEvent(Long clubMemberId) {
    }

    @Builder
    public record DeletedClubEvent(Long clubId) {
    }

    @Builder
    public record DeleteClubImageEvent(String imageUrl) {
    }
}
