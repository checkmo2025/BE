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
}
