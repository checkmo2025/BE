package checkmo.clubMeeting;

import lombok.Builder;

public class ClubMeetingEvent {
    @Builder
    public record ClubMeetingCreatedEvent(Long clubId, Long meetingId, Long version, String title, String content) {
    }

}
