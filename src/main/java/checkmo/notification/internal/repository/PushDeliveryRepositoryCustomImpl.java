package checkmo.notification.internal.repository;

import static checkmo.notification.internal.entity.QPushDelivery.pushDelivery;

import checkmo.notification.internal.entity.DeliveryStatus;
import checkmo.notification.internal.entity.PushDelivery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
public class PushDeliveryRepositoryCustomImpl implements PushDeliveryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    /**
     * PENDING 또는 재시도 기한이 지난 RETRY_WAIT 레코드를 PROCESSING으로 원자 전환한다.
     * FOR UPDATE SKIP LOCKED는 QueryDSL이 미지원이므로 native query로 ID만 선점하고,
     * 이후 상태 변경과 엔티티 조회는 QueryDSL로 처리한다.
     */
    @Override
    @Transactional
    public List<PushDelivery> claimPendingBatch(int limit, LocalDateTime now) {
        List<Long> ids = lockClaimableIds(limit, now);
        if (ids.isEmpty()) {
            return List.of();
        }

        markAsProcessing(ids, now);
        return fetchWithAssociations(ids);
    }

    @SuppressWarnings("unchecked")
    private List<Long> lockClaimableIds(int limit, LocalDateTime now) {
        return em.createNativeQuery(
                        "SELECT id FROM push_delivery " +
                        "WHERE status = 'PENDING' " +
                        "   OR (status = 'RETRY_WAIT' AND next_attempt_at <= :now) " +
                        "LIMIT :limit " +
                        "FOR UPDATE SKIP LOCKED"
                )
                .setParameter("now", now)
                .setParameter("limit", limit)
                .getResultList();
    }

    private void markAsProcessing(List<Long> ids, LocalDateTime now) {
        queryFactory.update(pushDelivery)
                .set(pushDelivery.status, DeliveryStatus.PROCESSING)
                .set(pushDelivery.processingStartedAt, now)
                .where(pushDelivery.id.in(ids))
                .execute();
    }

    private List<PushDelivery> fetchWithAssociations(List<Long> ids) {
        return queryFactory.selectFrom(pushDelivery)
                .join(pushDelivery.notification).fetchJoin()
                .join(pushDelivery.pushDevice).fetchJoin()
                .where(pushDelivery.id.in(ids))
                .fetch();
    }
}
