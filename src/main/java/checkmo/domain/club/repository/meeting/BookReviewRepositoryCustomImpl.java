package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.QClubMember;
import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.QBookReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookReviewRepositoryCustomImpl implements BookReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBookReview bookReview = QBookReview.bookReview;
    private final QClubMember clubMember = QClubMember.clubMember;


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
