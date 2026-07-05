package checkmo.notification.internal.scheduler;

import checkmo.notification.internal.entity.DeliveryStatus;
import checkmo.notification.internal.entity.PushDelivery;
import checkmo.notification.internal.entity.PushDevice;
import checkmo.notification.internal.repository.PushDeliveryRepository;
import checkmo.notification.internal.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushCleanupScheduler {

    private static final int PAGE_SIZE = 500;
    private static final int RETENTION_DAYS = 90;
    private static final List<DeliveryStatus> TERMINAL_STATUSES = List.of(
            DeliveryStatus.DELIVERED,
            DeliveryStatus.CANCELLED,
            DeliveryStatus.PERMANENT_FAILED
    );

    private final PushDeliveryRepository pushDeliveryRepository;
    private final PushDeviceRepository pushDeviceRepository;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        log.info("push 정리 시작: threshold={}", threshold);

        deleteExpiredDevices(threshold);
        deleteTerminalDeliveries(threshold);

        log.info("push 정리 완료");
    }

    // FK 순서: delivery 먼저 삭제 후 device 삭제
    public void deleteExpiredDevices(LocalDateTime threshold) {
        int totalDeleted = 0;
        List<PushDevice> page;

        do {
            page = pushDeviceRepository.findAllByActiveFalseAndDeactivatedAtBefore(
                    threshold, PageRequest.of(0, PAGE_SIZE)
            );
            if (page.isEmpty()) break;

            List<Long> deviceIds = page.stream().map(PushDevice::getId).toList();
            pushDeliveryRepository.deleteAllByPushDeviceIdIn(deviceIds);
            pushDeviceRepository.deleteAllByIdInBatch(deviceIds);
            totalDeleted += deviceIds.size();
        } while (page.size() == PAGE_SIZE);

        if (totalDeleted > 0) {
            log.info("비활성 device 삭제: {}건", totalDeleted);
        }
    }

    public void deleteTerminalDeliveries(LocalDateTime threshold) {
        int totalDeleted = 0;
        List<PushDelivery> page;

        do {
            page = pushDeliveryRepository.findTerminalDeliveriesOlderThan(
                    TERMINAL_STATUSES, threshold, PageRequest.of(0, PAGE_SIZE)
            );
            if (page.isEmpty()) break;

            List<Long> ids = page.stream().map(PushDelivery::getId).toList();
            pushDeliveryRepository.deleteAllByIdInBatch(ids);
            totalDeleted += ids.size();
        } while (page.size() == PAGE_SIZE);

        if (totalDeleted > 0) {
            log.info("만료 delivery 삭제: {}건", totalDeleted);
        }
    }
}
