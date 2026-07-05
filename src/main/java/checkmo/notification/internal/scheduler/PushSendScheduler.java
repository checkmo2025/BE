package checkmo.notification.internal.scheduler;

import checkmo.infra.PushAPI;
import checkmo.infra.PushSendRequest;
import checkmo.infra.PushSendResult;
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
    private static final int MAX_ATTEMPTS = 5;

    private final PushDeliveryRepository pushDeliveryRepository;
    private final PushDeviceRepository pushDeviceRepository;
    private final PushMessageFactory pushMessageFactory;
    private final PushAPI pushAPI;

    @Scheduled(fixedDelay = 5_000)
    public void sendPendingDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        List<PushDelivery> batch = pushDeliveryRepository.claimPendingBatch(BATCH_SIZE, now);
        if (batch.isEmpty()) {
            return;
        }

        List<PushSendRequest> requests = buildRequests(batch);
        List<PushSendResult> results = pushAPI.sendBatch(requests);

        for (int i = 0; i < batch.size(); i++) {
            PushSendResult result = (i < results.size()) ? results.get(i) : null;
            try {
                applySendResult(batch.get(i), result);
            } catch (Exception e) {
                log.error("push_delivery 상태 저장 실패: deliveryId={}", batch.get(i).getId(), e);
            }
        }
    }

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

    private List<PushSendRequest> buildRequests(List<PushDelivery> batch) {
        List<PushSendRequest> requests = new ArrayList<>(batch.size());
        for (PushDelivery delivery : batch) {
            try {
                requests.add(pushMessageFactory.build(delivery, delivery.getNotification()));
            } catch (Exception e) {
                log.error("push 메시지 조립 실패: deliveryId={}", delivery.getId(), e);
                requests.add(null);
            }
        }
        return requests;
    }

    private void applySendResult(PushDelivery delivery, PushSendResult result) {
        if (result == null) {
            handleRetryOrFail(delivery, "SEND_FAILED", "Expo API request failed or message build error");
        } else if (result.ok()) {
            delivery.markTicketAccepted(result.ticketId());
        } else if (result.isDeviceNotRegistered()) {
            delivery.getPushDevice().deactivate();
            pushDeviceRepository.save(delivery.getPushDevice());
            delivery.markCancelled();
        } else {
            String errorCode = result.errorCode() != null ? result.errorCode() : "UNKNOWN";
            handleRetryOrFail(delivery, errorCode, result.errorMessage());
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
