package checkmo.clubNotice.internal.listener;

import checkmo.clubManagement.ClubManagementEvent.DeletedClubEvent;
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
        clubNoticeCommandService.deleteAll(event.clubId());
    }
}
