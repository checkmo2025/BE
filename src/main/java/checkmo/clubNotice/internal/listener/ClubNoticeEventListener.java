package checkmo.clubNotice.internal.listener;

import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreatedEvent;
import checkmo.clubNotice.internal.service.command.ClubNoticeCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClubNoticeEventListener {

    private final ClubNoticeCommandService clubNoticeCommandService;

    @ApplicationModuleListener
    public void handleClubMeetingCreatedEvent(ClubMeetingCreatedEvent event) {
        try {
            clubNoticeCommandService.createMeetingNotice(event);
        } catch (Exception e) {
            log.error("독서 클럽 공지 생성 실패, ClubNoticeCreatedEvent: {}", event, e);
            throw e;
        }
    }
}
