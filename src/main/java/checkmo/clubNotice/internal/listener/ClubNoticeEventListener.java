package checkmo.clubNotice.internal.listener;

import checkmo.clubManagement.ClubManagementEvent.DeletedClubEvent;
import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingDeleted;
import checkmo.clubNotice.internal.service.command.ClubNoticeCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubNoticeEventListener {
    private final ClubNoticeCommandService clubNoticeCommandService;

    @ApplicationModuleListener
    public void handleDeletedClubEvent(DeletedClubEvent event) {
        clubNoticeCommandService.deleteAllByClubId(event.clubId());
    }

    @ApplicationModuleListener
    public void handleDeletedMeetingEvent(ClubMeetingDeleted event) {
        clubNoticeCommandService.deleteAllByMeetingId(event.clubId(), event.meetingId());
    }
}
