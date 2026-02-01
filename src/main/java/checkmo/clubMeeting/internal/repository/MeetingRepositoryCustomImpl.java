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

}
