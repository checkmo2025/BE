package checkmo.notification.internal.repository;

import static checkmo.notification.internal.entity.QNotification.notification;

import checkmo.notification.internal.entity.Notification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findNotificationPreviews(String receiverId, int pageSize) {
        return queryFactory
                .selectFrom(notification)
                .where(
                        notification.receiverId.eq(receiverId),
                        notification.isRead.eq(false)
                )
                .orderBy(notification.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<Notification> findNotifications(String receiverId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(notification)
                .where(
                        notification.receiverId.eq(receiverId),
                        cursorCondition(cursorId)
                )
                .orderBy(notification.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId != null ? notification.id.lt(cursorId) : null;
    }
}
