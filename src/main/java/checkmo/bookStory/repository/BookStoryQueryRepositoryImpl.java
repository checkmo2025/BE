package checkmo.bookStory.repository;

import static checkmo.bookStory.entity.QBookStory.bookStory;
import static checkmo.clubManagement.entity.QClubMember.clubMember;
import static checkmo.member.entity.QFollow.follow;

import checkmo.bookStory.entity.BookStory;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.clubManagement.entity.ClubMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookStoryQueryRepositoryImpl implements BookStoryQueryRepository {

    private final JPAQueryFactory JpaQueryFactory;

    @Override
    public List<BookStory> searchBookStories(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId,
                                             String targetMemberId, Long cursorId, int pageSize) {
        return switch (scope) {
            case ALL -> findAllBookStories(cursorId, pageSize);
            case MY -> findMyBookStories(memberId, cursorId, pageSize);
            case FOLLOWING -> findFollowBookStories(memberId, cursorId, pageSize);
            case CLUB -> findClubBookStories(memberId, clubId, cursorId, pageSize);
            case TARGET -> findTargetMemberBookStories(targetMemberId, cursorId, pageSize);
        };
    }

    private List<BookStory> findAllBookStories(Long cursorId, int pageSize) {

        return JpaQueryFactory
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

        return JpaQueryFactory
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
        return JpaQueryFactory
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

        if (!isMemberInClub(memberId, clubId)) {
            throw new IllegalArgumentException("해당 클럽의 멤버가 아닙니다.");
        }

        List<String> clubMemberIds = getClubMemberIds(clubId);
        if (clubMemberIds.isEmpty()) {
            return List.of();
        }

        return JpaQueryFactory
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

        return JpaQueryFactory
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

    private boolean isMemberInClub(String memberId, Long clubId) {
        return JpaQueryFactory
                .selectFrom(clubMember)
                .where(clubMember.clubId.eq(clubId)
                        .and(clubMember.memberId.eq(memberId))
                        .and(clubMember.clubMemberStatus.in(
                                ClubMember.ClubMemberStatus.MEMBER,
                                ClubMember.ClubMemberStatus.STAFF
                        )))
                .fetchFirst() != null;
    }

    private List<String> getFollowingMemberIds(String memberId) {
        return JpaQueryFactory
                .select(follow.followingId)
                .from(follow)
                .where(follow.followerId.eq(memberId))
                .fetch();
    }

    private List<String> getClubMemberIds(Long clubId) {
        return JpaQueryFactory
                .select(clubMember.memberId)
                .from(clubMember)
                .where(clubMember.clubId.eq(clubId)
                        .and(clubMember.clubMemberStatus.in(
                                ClubMember.ClubMemberStatus.MEMBER,
                                ClubMember.ClubMemberStatus.STAFF
                        )))
                .fetch();
    }
}
