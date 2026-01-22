package checkmo.news.internal.repository;

import static checkmo.news.internal.entity.QNews.news;

import checkmo.news.internal.entity.News;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class NewsQueryRepositoryImpl implements NewsQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<News> searchNews(Long cursorId, int pageSize) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return queryFactory
                .selectFrom(news)
                .where(
                        isPublished(today),
                        createCursorExp(cursorId)
                )
                .orderBy(news.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public List<News> searchNewsForAdmin(Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(news)
                .where(createCursorExp(cursorId))
                .orderBy(news.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression isPublished(LocalDate today) {
        return news.publishStartAt.loe(today)
                .and(news.publishEndAt.goe(today));
    }

    private BooleanExpression createCursorExp(Long cursorId) {
        return cursorId != null ? news.id.lt(cursorId) : null;
    }
}