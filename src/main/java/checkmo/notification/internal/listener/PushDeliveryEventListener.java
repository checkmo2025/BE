package checkmo.notification.internal.listener;

import checkmo.notification.internal.entity.DeliveryStatus;
import checkmo.notification.internal.entity.PushDelivery;
import checkmo.notification.internal.entity.PushDevice;
import checkmo.notification.internal.listener.event.NotificationCreatedForPush;
import checkmo.notification.internal.repository.NotificationRepository;
import checkmo.notification.internal.repository.PushDeliveryRepository;
import checkmo.notification.internal.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PushDeliveryEventListener {

    private final NotificationRepository notificationRepository;
    private final PushDeviceRepository pushDeviceRepository;
    private final PushDeliveryRepository pushDeliveryRepository;

    @ApplicationModuleListener
    public void handleNotificationCreated(NotificationCreatedForPush event) {
        List<PushDevice> devices = pushDeviceRepository.findAllByMemberIdAndActiveTrue(event.receiverId());
        if (devices.isEmpty()) {
            return;
        }

        notificationRepository.findById(event.notificationId()).ifPresent(notification -> {
            for (PushDevice device : devices) {
                PushDelivery delivery = PushDelivery.builder()
                        .notification(notification)
                        .pushDevice(device)
                        .status(DeliveryStatus.PENDING)
                        .build();
                try {
                    pushDeliveryRepository.save(delivery);
                } catch (DataIntegrityViolationException e) {
                    log.debug("push_delivery 이미 존재: notificationId={}, deviceId={}", event.notificationId(), device.getId());
                }
            }
        });
    }
}
