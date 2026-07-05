package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.DeliveryStatus;
import checkmo.notification.internal.entity.PushDelivery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PushDeliveryRepository extends JpaRepository<PushDelivery, Long>, PushDeliveryRepositoryCustom {

    Optional<PushDelivery> findByNotificationIdAndPushDeviceId(Long notificationId, Long pushDeviceId);

    // sentAt 기준으로 조회해 receipt 확인 시점을 Expo 권장 15분 지연에 맞춘다
    @Query("""
            SELECT pd FROM PushDelivery pd
            JOIN FETCH pd.pushDevice
            WHERE pd.status = checkmo.notification.internal.entity.DeliveryStatus.TICKET_ACCEPTED
              AND pd.expoTicketId IS NOT NULL
              AND pd.sentAt <= :threshold
            ORDER BY pd.sentAt ASC
            """)
    List<PushDelivery> findTicketAcceptedOlderThan(@Param("threshold") LocalDateTime threshold, Pageable pageable);

    // processingStartedAt 기준으로 lease 만료된 PROCESSING을 찾아 RETRY_WAIT으로 복구한다
    @Query("""
            SELECT pd FROM PushDelivery pd
            WHERE pd.status = checkmo.notification.internal.entity.DeliveryStatus.PROCESSING
              AND pd.processingStartedAt <= :leaseExpiry
            """)
    List<PushDelivery> findStaleProcessing(@Param("leaseExpiry") LocalDateTime leaseExpiry);

    @Query("""
            SELECT pd FROM PushDelivery pd
            WHERE pd.pushDevice.id = :pushDeviceId
              AND pd.status NOT IN :terminalStatuses
            """)
    List<PushDelivery> findActiveDeliveriesByPushDeviceId(
            @Param("pushDeviceId") Long pushDeviceId,
            @Param("terminalStatuses") List<DeliveryStatus> terminalStatuses
    );

    void deleteAllByNotificationIdIn(List<Long> notificationIds);

    void deleteAllByPushDeviceIdIn(List<Long> pushDeviceIds);
}
