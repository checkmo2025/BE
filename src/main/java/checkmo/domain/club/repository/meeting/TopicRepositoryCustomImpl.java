package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.QTopic;
import checkmo.domain.club.entity.meeting.Topic;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TopicRepositoryCustomImpl implements TopicRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QTopic topic = QTopic.topic;

    @Override
    public List<Topic> findTopicsByCursorAsc(Long meetingId, Long cursorId, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(topic.meeting.id.eq(meetingId));

        if (cursorId != null) {
            predicate.and(topic.id.gt(cursorId));
        }
        return queryFactory
                .selectFrom(topic)
                .distinct()
                .where(predicate)
                .join(topic.clubMember).fetchJoin()
                .orderBy(topic.id.asc())
                .limit(size)
                .fetch();
    }
}
