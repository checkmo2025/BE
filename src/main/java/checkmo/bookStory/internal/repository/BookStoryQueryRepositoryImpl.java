package checkmo.bookStory.internal.repository;

import static checkmo.bookStory.internal.entity.QBookStory.bookStory;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.member.MemberAPI;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class BookStoryQueryRepositoryImpl implements BookStoryQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ClubManagementAPI clubManagementAPI;
    private final MemberAPI memberAPI;

    @Override
    public List<BookStory> searchBookStories(
            String memberId,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            String targetMemberId,
            Long cursorId,
            int pageSize
    ) {
        return switch (scope) {
            case ALL -> findAllBookStories(cursorId, pageSize);
            case MY -> findMyBookStories(memberId, cursorId, pageSize);
            case FOLLOWING -> findFollowBookStories(memberId, cursorId, pageSize);
            case CLUB -> findClubBookStories(memberId, clubId, cursorId, pageSize);
            case TARGET -> findTargetMemberBookStories(targetMemberId, cursorId, pageSize);
        };
    }

    private List<BookStory> findAllBookStories(Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(bookStory)
                .where(createCursorExp(cursorId))
                .orderBy(bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private List<BookStory> findFollowBookStories(String memberId, Long cursorId, int pageSize) {
        List<String> followingMemberIds = getFollowingMemberIds(memberId);

        if (followingMemberIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(bookStory)
                .where(
                        createCursorExp(cursorId),
                        bookStory.memberId.in(followingMemberIds)
                )
                .orderBy(bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private List<BookStory> findMyBookStories(String memberId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(bookStory)
                .where(
                        createCursorExp(cursorId),
                        bookStory.memberId.eq(memberId)
                )
                .orderBy(bookStory.id.desc())
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
                        createCursorExp(cursorId),
                        bookStory.memberId.in(clubMemberIds)
                )
                .orderBy(bookStory.id.desc())
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
                        createCursorExp(cursorId),
                        bookStory.memberId.eq(targetMemberId)
                )
                .orderBy(bookStory.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression createCursorExp(Long cursorId) {
        return cursorId != null ? bookStory.id.lt(cursorId) : null;
    }

    private void validateClubMember(String memberId, Long clubId) {
        clubManagementAPI.fetchActiveClubMemberId(clubId, memberId);
    }

    private List<String> getFollowingMemberIds(String memberId) {
        return memberAPI.getFollowingMemberIds(memberId);
    }

    private List<String> getClubMemberIds(Long clubId) {
        return clubManagementAPI.fetchActiveMemberIds(clubId);
    }
}
