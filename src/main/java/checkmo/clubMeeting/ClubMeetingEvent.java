package checkmo.clubMeeting;

import lombok.Builder;

public class ClubMeetingEvent {
    @Builder
    public record ClubMeetingCreatedEvent(Long clubId, Long meetingId, String title, String content) {
    }

}
