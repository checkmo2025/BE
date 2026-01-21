package checkmo.clubNotice;

import java.util.List;
import lombok.Builder;

public class ClubNoticeEvent {

    @Builder
    public record ClubNoticeCreated(Long eventId, Long clubId, String clubName) {
    }

    @Builder
    public record DeleteNoticeImage(List<String> imageUrls) {
    }
}