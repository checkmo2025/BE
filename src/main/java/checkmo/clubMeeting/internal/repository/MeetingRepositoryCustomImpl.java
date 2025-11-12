package checkmo.clubMeeting.internal.repository;

import static checkmo.clubMeeting.internal.entity.QMeeting.meeting;

import checkmo.clubMeeting.internal.entity.Meeting;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MeetingRepositoryCustomImpl implements MeetingRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Meeting> findAllByClubIdAndCursorDesc(Long clubId, Long cursorId, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(meeting.clubId.eq(clubId));
        if (cursorId != null) {
            predicate.and(meeting.id.lt(cursorId));
        }

        return queryFactory
                .selectFrom(meeting)
                .distinct()
                .where(predicate)
                .orderBy(meeting.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<Meeting> findAllByClubIdAndGenerationAndCursorDesc(Long clubId, Integer generation, Long cursorId,
                                                                   Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(meeting.clubId.eq(clubId));

        // 기수가 null이면 최신 기수의 모임을 조회, null이 아니면 해당 기수의 모임을 조회
        if (generation != null) {
            predicate.and(meeting.generation.eq(generation));
        } else {
            predicate.and(
                    meeting.generation.eq(
                            com.querydsl.jpa.JPAExpressions
                                    .select(meeting.generation.max())
                                    .from(meeting)
                                    .where(meeting.clubId.eq(clubId))
                    )
            );
        }

        if (cursorId != null) {
            predicate.and(meeting.id.lt(cursorId));
        }

        return queryFactory
                .selectFrom(meeting)
                .distinct()
                .where(predicate)
                .orderBy(meeting.id.desc())
                .limit(size)
                .fetch();
    }
}
