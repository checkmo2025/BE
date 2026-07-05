package checkmo.notification.internal.scheduler;

import checkmo.infra.PushAPI;
import checkmo.infra.PushReceiptResult;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushReceiptScheduler {

    private static final int RECEIPT_BATCH_SIZE = 300;
    private static final int PAGE_SIZE = 1000;
    private static final int MAX_ATTEMPTS = 5;

    private final PushDeliveryRepository pushDeliveryRepository;
    private final PushDeviceRepository pushDeviceRepository;
    private final PushAPI pushAPI;

    @Scheduled(fixedDelay = 900_000)
    public void checkReceipts() {
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

        Map<String, PushReceiptResult> receipts = fetchAllReceipts(new ArrayList<>(byTicketId.keySet()));

        for (PushDelivery delivery : deliveries) {
            try {
                applyReceiptResult(delivery, receipts.get(delivery.getExpoTicketId()), now);
            } catch (Exception e) {
                log.error("receipt 처리 실패: deliveryId={}, ticketId={}",
                        delivery.getId(), delivery.getExpoTicketId(), e);
            }
        }
    }

    private Map<String, PushReceiptResult> fetchAllReceipts(List<String> ticketIds) {
        Map<String, PushReceiptResult> result = new HashMap<>();
        for (int i = 0; i < ticketIds.size(); i += RECEIPT_BATCH_SIZE) {
            List<String> chunk = ticketIds.subList(i, Math.min(i + RECEIPT_BATCH_SIZE, ticketIds.size()));
            result.putAll(pushAPI.getReceipts(chunk));
        }
        return result;
    }

    private void applyReceiptResult(PushDelivery delivery, PushReceiptResult receipt, LocalDateTime now) {
        if (receipt == null || receipt.isPending()) {
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
            handleRetryOrFail(delivery, receipt.errorCode(), receipt.errorMessage());
        } else if (receipt.isPermanentError()) {
            delivery.markPermanentFailed(receipt.errorCode(), receipt.errorMessage());
        } else {
            handleRetryOrFail(delivery, receipt.errorCode(), receipt.errorMessage());
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
