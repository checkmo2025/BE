package checkmo.clubMeeting.internal.repository;

import static checkmo.clubManagement.internal.entity.QClubMember.clubMember;
import static checkmo.clubMeeting.internal.entity.QBookReview.bookReview;

import checkmo.clubMeeting.internal.entity.BookReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookReviewRepositoryCustomImpl implements BookReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<BookReview> findBookReviewsByCusor(Long meetingId, Long lastReviewId, int size) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(bookReview.meeting.id.eq(meetingId));

        if (lastReviewId != null) {
            predicate.and(bookReview.id.lt(lastReviewId));
        }

        return queryFactory
                .selectFrom(bookReview)
                .distinct()
                .where(predicate)
                .join(bookReview.clubMember, clubMember)
                .fetchJoin() //review -> clubMember -> member 작성자 정보를 맵핑시키기 위해 fetchJoin
                .orderBy(bookReview.id.desc())
                .limit(size)
                .fetch();
    }
}
