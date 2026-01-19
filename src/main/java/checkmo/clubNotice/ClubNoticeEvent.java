package checkmo.clubNotice;

import lombok.Builder;

public class ClubNoticeEvent {

    @Builder
    public record ClubNoticeCreated(Long eventId, Long clubId, String clubName) {
    }
}