package checkmo.clubMeeting.internal.listener;

import checkmo.clubManagement.ClubManagementEvent.DeletedClubEvent;
import checkmo.clubMeeting.internal.service.command.ClubMeetingCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubMeetingEventListener {
    private final ClubMeetingCommandService clubMeetingCommandService;

    @ApplicationModuleListener
    public void handleDeletedClubEvent(DeletedClubEvent event) {
        clubMeetingCommandService.deleteAll(event.clubId());
    }
}
