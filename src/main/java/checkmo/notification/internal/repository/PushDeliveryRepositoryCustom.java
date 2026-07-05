package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.PushDelivery;

import java.time.LocalDateTime;
import java.util.List;

public interface PushDeliveryRepositoryCustom {

    List<PushDelivery> claimPendingBatch(int limit, LocalDateTime now);
}
