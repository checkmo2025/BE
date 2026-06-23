package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.BookStoryStatus;
import checkmo.bookStory.internal.repository.projection.BookStorySitemapProjection;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.clubManagement.ClubManagementAPI;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static checkmo.bookStory.internal.entity.QBookStory.bookStory;

@RequiredArgsConstructor
@Repository
public class BookStoryQueryRepositoryImpl implements BookStoryQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ClubManagementAPI clubManagementAPI;

    @Override
    public List<BookStory> searchBookStories(
            String memberId,
            List<String> excludedMemberIds,
            List<String> followingMemberIds,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            String targetMemberId,
            Long cursorId,
            int pageSize
    ) {
        return switch (scope) {
            case ALL -> findAllBookStories(excludedMemberIds, cursorId, pageSize);
            case MY -> findMyBookStories(memberId, cursorId, pageSize);
            case FOLLOWING -> findFollowBookStories(followingMemberIds, excludedMemberIds, cursorId, pageSize);
            case CLUB -> findClubBookStories(memberId, clubId, cursorId, pageSize);
            case TARGET -> findTargetMemberBookStories(targetMemberId, cursorId, pageSize);
        };
    }

    @Override
    public List<BookStory> searchBookStories(String bookId, List<String> excludedMemberIds, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(bookStory)
                .where(
                        notDeleted(),
                        published(),
                        createPublicCursorExp(cursorId),
                        notInExcludedMemberIds(excludedMemberIds),
                        bookStory.bookId.eq(bookId)
                )
                .orderBy(bookStory.createdAt.desc(), bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Page<BookStory> searchBookStoriesForAdmin(String keyword, Pageable pageable) {
        List<BookStory> content = queryFactory
                .selectFrom(bookStory)
                .where(
                        notDeleted(),
                        published(),
                        containsTitle(keyword)
                )
                .orderBy(bookStory.createdAt.desc(), bookStory.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(bookStory.count())
                .from(bookStory)
                .where(
                        notDeleted(),
                        published(),
                        containsTitle(keyword)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public List<BookStorySitemapProjection> findPublishedSitemapItems(Long cursorId, int pageSize) {
        return queryFactory
                .select(Projections.constructor(
                        BookStorySitemapProjection.class,
                        bookStory.id,
                        bookStory.updatedAt
                ))
                .from(bookStory)
                .where(
                        notDeleted(),
                        published(),
                        createSitemapCursorExp(cursorId)
                )
                .orderBy(bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private List<BookStory> findAllBookStories(List<String> excludedMemberIds, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(bookStory)
                .where(
                        notDeleted(),
                        published(),
                        createPublicCursorExp(cursorId),
                        notInExcludedMemberIds(excludedMemberIds)
                )
                .orderBy(bookStory.createdAt.desc(), bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private List<BookStory> findFollowBookStories(
            List<String> followingMemberIds,
            List<String> excludedMemberIds,
            Long cursorId,
            int pageSize
    ) {
        if (followingMemberIds == null || followingMemberIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(bookStory)
                .where(
                        notDeleted(),
                        published(),
                        createPublicCursorExp(cursorId),
                        bookStory.memberId.in(followingMemberIds),
                        notInExcludedMemberIds(excludedMemberIds)
                )
                .orderBy(bookStory.createdAt.desc(), bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private List<BookStory> findMyBookStories(String memberId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(bookStory)
                .where(
                        notDeleted(),
                        createMyCursorExp(cursorId),
                        bookStory.memberId.eq(memberId)
                )
                .orderBy(bookStory.status.asc(), bookStory.createdAt.desc(), bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private List<BookStory> findClubBookStories(String memberId, Long clubId, Long cursorId, int pageSize) {
        if (clubId == null) {
            throw new IllegalArgumentException("scope가 club인 경우 clubId는 필수입니다.");
        }

        validateClubMember(memberId, clubId);

        List<String> clubMemberIds = getClubMemberIds(clubId);
        if (clubMemberIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(bookStory)
                .where(
                        notDeleted(),
                        published(),
                        createPublicCursorExp(cursorId),
                        bookStory.memberId.in(clubMemberIds)
                )
                .orderBy(bookStory.createdAt.desc(), bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private List<BookStory> findTargetMemberBookStories(String targetMemberId, Long cursorId, int pageSize) {
        if (targetMemberId == null) {
            throw new IllegalArgumentException("scope가 target인 경우 targetMemberId는 필수입니다.");
        }

        return queryFactory
                .selectFrom(bookStory)
                .where(
                        notDeleted(),
                        published(),
                        createPublicCursorExp(cursorId),
                        bookStory.memberId.eq(targetMemberId)
                )
                .orderBy(bookStory.createdAt.desc(), bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression notDeleted() {
        return bookStory.deleted.eq(false);
    }

    private BooleanExpression published() {
        return bookStory.status.eq(BookStoryStatus.PUBLISHED);
    }

    private BooleanExpression createPublicCursorExp(Long cursorId) {
        BookStory cursor = findCursor(cursorId);
        if (cursor == null) {
            return null;
        }

        return olderThanCursor(cursor.getCreatedAt(), cursor.getId());
    }

    private BooleanExpression createMyCursorExp(Long cursorId) {
        BookStory cursor = findCursor(cursorId);
        if (cursor == null) {
            return null;
        }

        BooleanExpression olderInSameStatus = bookStory.status.eq(cursor.getStatus())
                .and(olderThanCursor(cursor.getCreatedAt(), cursor.getId()));

        if (cursor.getStatus() == BookStoryStatus.DRAFT) {
            return olderInSameStatus.or(bookStory.status.eq(BookStoryStatus.PUBLISHED));
        }

        return olderInSameStatus;
    }

    private BooleanExpression createSitemapCursorExp(Long cursorId) {
        return cursorId == null ? null : bookStory.id.lt(cursorId);
    }

    private BooleanExpression olderThanCursor(LocalDateTime createdAt, Long id) {
        return bookStory.createdAt.lt(createdAt)
                .or(bookStory.createdAt.eq(createdAt).and(bookStory.id.lt(id)));
    }

    private BookStory findCursor(Long cursorId) {
        if (cursorId == null) {
            return null;
        }

        return queryFactory
                .selectFrom(bookStory)
                .where(bookStory.id.eq(cursorId))
                .fetchOne();
    }

    private BooleanExpression containsTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return bookStory.title.containsIgnoreCase(keyword.trim());
    }

    private BooleanExpression notInExcludedMemberIds(List<String> excludedMemberIds) {
        return excludedMemberIds == null || excludedMemberIds.isEmpty() ? null : bookStory.memberId.notIn(excludedMemberIds);
    }

    private void validateClubMember(String memberId, Long clubId) {
        clubManagementAPI.validateAndFetchActiveClubMemberId(clubId, memberId);
    }

    private List<String> getClubMemberIds(Long clubId) {
        return clubManagementAPI.fetchActiveMemberIds(clubId);
    }
}
