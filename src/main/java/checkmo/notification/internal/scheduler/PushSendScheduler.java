package checkmo.notification.internal.scheduler;

import checkmo.infra.push.ExpoMessage;
import checkmo.infra.push.ExpoPushClient;
import checkmo.infra.push.ExpoTicket;
import checkmo.notification.internal.PushMessageFactory;
import checkmo.notification.internal.entity.PushDelivery;
import checkmo.notification.internal.repository.PushDeliveryRepository;
import checkmo.notification.internal.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushSendScheduler {

    private static final int BATCH_SIZE = 100;
    // 5회 시도 후 영구 실패: attemptCount가 이 값에 도달하면 PERMANENT_FAILED
    private static final int MAX_ATTEMPTS = 5;

    private final PushDeliveryRepository pushDeliveryRepository;
    private final PushDeviceRepository pushDeviceRepository;
    private final PushMessageFactory pushMessageFactory;
    private final ExpoPushClient expoPushClient;

    @Scheduled(fixedDelay = 5_000)
    public void sendPendingDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        List<PushDelivery> batch = pushDeliveryRepository.claimPendingBatch(BATCH_SIZE, now);
        if (batch.isEmpty()) {
            return;
        }

        List<ExpoMessage> messages = buildMessages(batch);
        List<ExpoTicket> tickets = expoPushClient.sendBatch(messages);

        for (int i = 0; i < batch.size(); i++) {
            ExpoTicket ticket = (i < tickets.size()) ? tickets.get(i) : null;
            try {
                applyTicketResult(batch.get(i), ticket);
            } catch (Exception e) {
                log.error("push_delivery 상태 저장 실패: deliveryId={}", batch.get(i).getId(), e);
            }
        }
    }

    // lease 10분 초과 PROCESSING을 RETRY_WAIT으로 복구
    @Scheduled(fixedDelay = 60_000)
    public void recoverStaleProcessing() {
        LocalDateTime leaseExpiry = LocalDateTime.now().minusMinutes(10);
        List<PushDelivery> stale = pushDeliveryRepository.findStaleProcessing(leaseExpiry);

        for (PushDelivery delivery : stale) {
            try {
                handleRetryOrFail(delivery, "STALE_PROCESSING", "Processing lease expired");
                pushDeliveryRepository.save(delivery);
            } catch (Exception e) {
                log.error("stale PROCESSING 복구 실패: deliveryId={}", delivery.getId(), e);
            }
        }
    }

    private List<ExpoMessage> buildMessages(List<PushDelivery> batch) {
        List<ExpoMessage> messages = new ArrayList<>(batch.size());
        for (PushDelivery delivery : batch) {
            try {
                messages.add(pushMessageFactory.build(delivery, delivery.getNotification()));
            } catch (Exception e) {
                log.error("push 메시지 조립 실패: deliveryId={}", delivery.getId(), e);
                messages.add(null);
            }
        }
        return messages;
    }

    private void applyTicketResult(PushDelivery delivery, ExpoTicket ticket) {
        if (ticket == null) {
            // 메시지 조립 실패 또는 HTTP 오류 → RETRY_WAIT
            handleRetryOrFail(delivery, "SEND_FAILED", "Expo API request failed or message build error");
        } else if ("ok".equals(ticket.status())) {
            delivery.markTicketAccepted(ticket.id());
        } else if (ticket.isDeviceNotRegistered()) {
            // device를 비활성화하고 delivery는 CANCELLED
            delivery.getPushDevice().deactivate();
            pushDeviceRepository.save(delivery.getPushDevice());
            delivery.markCancelled();
        } else {
            String errorCode = ticket.details() != null ? ticket.details().error() : "UNKNOWN";
            handleRetryOrFail(delivery, errorCode, ticket.message());
        }

        pushDeliveryRepository.save(delivery);
    }

    private void handleRetryOrFail(PushDelivery delivery, String errorCode, String errorMessage) {
        if (delivery.getAttemptCount() >= MAX_ATTEMPTS - 1) {
            delivery.markPermanentFailed(errorCode, errorMessage);
        } else {
            delivery.markRetryWait(nextAttemptAt(delivery.getAttemptCount()), errorCode, errorMessage);
        }
    }

    // attemptCount는 markRetryWait 호출 전 값 (0-based)
    private LocalDateTime nextAttemptAt(int attemptCount) {
        Duration delay = switch (attemptCount) {
            case 0 -> Duration.ofMinutes(1);
            case 1 -> Duration.ofMinutes(5);
            case 2 -> Duration.ofMinutes(15);
            default -> Duration.ofMinutes(60);
        };
        return LocalDateTime.now().plus(delay);
    }
}
