package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.entity.meeting.QMeeting;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetingRepositoryCustomImpl implements MeetingRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QMeeting meeting = QMeeting.meeting;

    @Override
    public List<Meeting> findMeetingsByClubIdAndCursorDesc(Long clubId, Long cursorId, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(meeting.club.id.eq(clubId));
        if (cursorId != null) {
            predicate.and(meeting.id.lt(cursorId));
        }

        return queryFactory
                .selectFrom(meeting)
                .where(predicate)
                .orderBy(meeting.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<Meeting> findMeetingsByClubIdAndGenerationAndCursorDesc(Long clubId, Integer generation, Long cursorId, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(meeting.club.id.eq(clubId));

        // 기수가 null이면 최신 기수의 모임을 조회, null이 아니면 해당 기수의 모임을 조회
        if (generation != null) {
            predicate.and(meeting.generation.eq(generation));
        } else {
            predicate.and(
                    meeting.generation.eq(
                            com.querydsl.jpa.JPAExpressions
                                    .select(meeting.generation.max())
                                    .from(meeting)
                                    .where(meeting.club.id.eq(clubId))
                    )
            );
        }

        if (cursorId != null) {
            predicate.and(meeting.id.lt(cursorId));
        }

        return queryFactory
                .selectFrom(meeting)
                .where(predicate)
                .orderBy(meeting.id.desc())
                .limit(size)
                .fetch();
    }
}
