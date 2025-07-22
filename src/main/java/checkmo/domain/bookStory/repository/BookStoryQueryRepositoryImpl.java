package checkmo.domain.bookStory.repository;

import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.club.entity.ClubMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static checkmo.domain.bookStory.entity.QBookStory.bookStory;
import static checkmo.domain.club.entity.QClubMember.clubMember;

@Repository
@RequiredArgsConstructor
public class BookStoryQueryRepositoryImpl implements BookStoryQueryRepository {

    private final JPAQueryFactory JpaQueryFactory;

    @Override
    public List<BookStory> searchBookStories(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, Long cursorId, int pageSize) {
        return switch (scope) {
            case ALL -> findAllBookStories(cursorId, pageSize);
            case MY -> findMyBookStories(memberId, cursorId, pageSize);
            case CLUB -> findClubBookStories(memberId, clubId, cursorId, pageSize);
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
