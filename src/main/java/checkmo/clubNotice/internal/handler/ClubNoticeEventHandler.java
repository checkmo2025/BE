package checkmo.clubNotice.internal.handler;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreatedEvent;
import checkmo.clubNotice.internal.service.command.ClubNoticeCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClubNoticeEventHandler {

    private final ClubNoticeCommandService clubNoticeCommandService;

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void handleClubMeetingCreatedEvent(ClubMeetingCreatedEvent event) {
        try {
            clubNoticeCommandService.createMeetingNotice(event);
        } catch (Exception e) {
            log.error("독서 클럽 공지 생성 실패, ClubNoticeCreatedEvent: {}", event, e);
        }
    }
}
