package checkmo.clubManagement;

import lombok.Builder;

public class ClubManagementEvent {

    @Builder
    public record ClubDeleted(Long clubId) {
    }
}
