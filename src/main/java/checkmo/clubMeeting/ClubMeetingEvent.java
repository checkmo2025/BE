package checkmo.clubMeeting;

import lombok.Builder;

public class ClubMeetingEvent {

    //TODO : 아래 두개의 이벤트 이름 수정 필요!!

    @Builder
    public record ClubMeetingCreatedEvent(Long clubId, Long meetingId, Long version, String title, String content) {
    }

    @Builder
    public record ClubMeetingCreated(Long eventId, Long clubId, String clubName) {
    }
}
