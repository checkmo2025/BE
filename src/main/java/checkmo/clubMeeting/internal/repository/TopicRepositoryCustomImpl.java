package checkmo.clubMeeting.internal.repository;

import static checkmo.clubMeeting.internal.entity.QTopic.topic;

import checkmo.clubMeeting.internal.entity.Topic;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TopicRepositoryCustomImpl implements TopicRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Topic> findAllByCursorOrderByIdDesc(Long meetingId, Long cursorId, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(topic.meetingId.eq(meetingId));

        if (cursorId != null) {
            predicate.and(topic.id.lt(cursorId));
        }

        JPAQuery<Topic> query = queryFactory
                .selectFrom(topic)
                .distinct()
                .where(predicate)
                .orderBy(topic.id.desc());

        if (size != null) {
            query.limit(size);
        }

        return query.fetch();
    }
}
