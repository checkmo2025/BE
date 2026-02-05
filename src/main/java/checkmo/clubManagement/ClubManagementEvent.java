package checkmo.clubManagement;

import lombok.Builder;

public class ClubManagementEvent {

    @Builder
    public record JoinClubEvent(Long eventId, String memberId, Long clubId, String clubName) {
    }

    @Builder
    public record LeaveClubEvent(Long clubMemberId) {
    }

    @Builder
    public record ClubDeletedEvent(Long clubId) {
    }

    @Builder
    public record DeleteClubImage(String imageUrl) {
    }
}
