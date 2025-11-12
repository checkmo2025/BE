package checkmo.clubManagement;

import lombok.Builder;

public class ClubManagementEvent {

    @Builder
    public record JoinClubEvent(String memberId, Long clubId, String clubName) {
    }

    @Builder
    public record ClubDeletedEvent(Long clubId) {
    }
}
