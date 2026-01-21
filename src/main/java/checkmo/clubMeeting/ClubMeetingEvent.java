package checkmo.clubMeeting;

import lombok.Builder;

public class ClubMeetingEvent {

    @Builder
    public record ClubMeetingCreated(Long eventId, Long clubId, String clubName) {
    }
}
