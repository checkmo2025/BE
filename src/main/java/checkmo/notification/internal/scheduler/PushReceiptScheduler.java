package checkmo.notification.internal.scheduler;

import checkmo.infra.push.ExpoReceipt;
import checkmo.infra.push.ExpoPushClient;
import checkmo.notification.internal.entity.PushDelivery;
import checkmo.notification.internal.repository.PushDeliveryRepository;
import checkmo.notification.internal.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushReceiptScheduler {

    // Expo receipt API 단일 요청 최대 ticket 수
    private static final int RECEIPT_BATCH_SIZE = 300;
    private static final int PAGE_SIZE = 1000;
    private static final int MAX_ATTEMPTS = 5;

    private final PushDeliveryRepository pushDeliveryRepository;
    private final PushDeviceRepository pushDeviceRepository;
    private final ExpoPushClient expoPushClient;

    @Scheduled(fixedDelay = 900_000)
    public void checkReceipts() {
        // sentAt 기준 15분 이상 지난 TICKET_ACCEPTED 조회
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(15);

        List<PushDelivery> deliveries = pushDeliveryRepository.findTicketAcceptedOlderThan(
                threshold, PageRequest.of(0, PAGE_SIZE)
        );
        if (deliveries.isEmpty()) {
            return;
        }

        Map<String, PushDelivery> byTicketId = deliveries.stream()
                .collect(Collectors.toMap(PushDelivery::getExpoTicketId, d -> d));

        Map<String, ExpoReceipt> receipts = fetchAllReceipts(new ArrayList<>(byTicketId.keySet()));

        for (PushDelivery delivery : deliveries) {
            try {
                applyReceiptResult(delivery, receipts.get(delivery.getExpoTicketId()), now);
            } catch (Exception e) {
                log.error("receipt 처리 실패: deliveryId={}, ticketId={}",
                        delivery.getId(), delivery.getExpoTicketId(), e);
            }
        }
    }

    // Expo 300건 제한에 맞춰 분할 요청 후 결과를 합친다
    private Map<String, ExpoReceipt> fetchAllReceipts(List<String> ticketIds) {
        Map<String, ExpoReceipt> result = new java.util.HashMap<>();
        for (int i = 0; i < ticketIds.size(); i += RECEIPT_BATCH_SIZE) {
            List<String> chunk = ticketIds.subList(i, Math.min(i + RECEIPT_BATCH_SIZE, ticketIds.size()));
            result.putAll(expoPushClient.getReceipts(chunk));
        }
        return result;
    }

    private void applyReceiptResult(PushDelivery delivery, ExpoReceipt receipt, LocalDateTime now) {
        if (receipt == null || "pending".equals(receipt.status())) {
            // receipt 미도착: 24시간 초과 시 만료 처리
            if (delivery.getSentAt() != null
                    && delivery.getSentAt().plus(Duration.ofHours(24)).isBefore(now)) {
                delivery.markPermanentFailed("RECEIPT_EXPIRED", "Receipt not arrived within 24 hours");
            } else {
                delivery.updateReceiptCheckedAt(now);
            }
        } else if (receipt.isOk()) {
            delivery.markDelivered();
        } else if (receipt.isDeviceNotRegistered()) {
            delivery.getPushDevice().deactivate();
            pushDeviceRepository.save(delivery.getPushDevice());
            delivery.markCancelled();
        } else if (receipt.isMessageRateExceeded()) {
            handleRetryOrFail(delivery, receipt.errorCode(), receipt.message());
        } else if (receipt.isPermanentError()) {
            delivery.markPermanentFailed(receipt.errorCode(), receipt.message());
        } else {
            // 알 수 없는 오류 → 재시도
            handleRetryOrFail(delivery, receipt.errorCode(), receipt.message());
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
