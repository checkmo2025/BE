package checkmo.club.repository.meeting;

import checkmo.club.entity.meeting.BookReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static checkmo.club.entity.meeting.QBookReview.bookReview;
import static checkmo.club.entity.QClubMember.clubMember;

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
                .join(bookReview.clubMember, clubMember).fetchJoin() //review -> clubMember -> member 작성자 정보를 맵핑시키기 위해 fetchJoin
                .orderBy(bookReview.id.desc())
                .limit(size)
                .fetch();
    }
}
