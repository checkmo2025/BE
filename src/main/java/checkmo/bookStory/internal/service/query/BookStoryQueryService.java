package checkmo.bookStory.internal.service.query;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.BookStoryStatus;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.repository.BookStoryLikedRepository;
import checkmo.bookStory.internal.repository.BookStoryRepository;
import checkmo.bookStory.internal.repository.CommentRepository;
import checkmo.bookStory.internal.repository.projection.BookStoryPrevNextProjection;
import checkmo.bookStory.internal.repository.projection.BookStorySitemapProjection;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryService {

    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;
    private final CommentRepository commentRepository;

    /**
     * 조건에 맞는 책 이야기 엔티티 목록을 조회 (페이지네이션을 위해 +1개 더 조회)
     *
     * @param memberId       조회하는 회원의 ID
     * @param scope          조회 범위 ("ALL", "MY", "FOLLOWING", "CLUB", "TARGET")
     * @param clubId         클럽 ID (scope가 "CLUB"일 때 필수)
     * @param targetMemberId 대상 회원 ID (scope가 "TARGET"일 때 필수)
     * @param cursorId       페이지네이션을 위한 커서 ID (처음에는 null)
     * @param pageSize       페이지 크기
     * @return 조회된 책 이야기 엔티티 목록
     */
    public List<BookStory> retrieveBookStories(
            String memberId,
            List<String> excludedMemberIds,
            List<String> followingMemberIds,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            String targetMemberId,
            Long cursorId,
            int pageSize
    ) {
        return bookStoryRepository.searchBookStories(
                memberId, excludedMemberIds, followingMemberIds, scope, clubId, targetMemberId, cursorId, pageSize
        );
    }

    public List<BookStory> retrieveBookStories(
            String bookId,
            List<String> excludedMemberIds,
            Long cursorId,
            int pageSize
    ) {
        return bookStoryRepository.searchBookStories(bookId, excludedMemberIds, cursorId, pageSize);
    }

    public Page<BookStory> retrieveBookStoriesForAdmin(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return bookStoryRepository.searchBookStoriesForAdmin(keyword, pageable);
    }

    public List<BookStorySitemapProjection> retrieveSitemapItems(Long cursorId, int pageSize) {
        return bookStoryRepository.findPublishedSitemapItems(cursorId, pageSize);
    }

    /**
     * 책 이야기 엔티티 조회 (삭제되지 않은 것만)
     *
     * @param bookStoryId 조회할 책 이야기의 ID
     * @return 조회된 책 이야기 엔티티
     */
    public BookStory retrieveBookStory(Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findByIdAndDeletedFalse(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));
        if (!bookStory.isPublished()) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND);
        }

        return bookStory;
    }

    public BookStory retrieveAccessibleBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findByIdAndDeletedFalse(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        if (bookStory.isDraft() && !bookStory.verifyOwner(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND);
        }

        return bookStory;
    }

    /**
     * 책 이야기의 부모 댓글 조회 (대댓글 포함안됨)
     *
     * @param bookStoryId 조회할 책 이야기 ID
     * @return 조회된 댓글 목록
     */
    public List<Comment> retrieveBookStoryComments(Long bookStoryId) {
        return commentRepository.findParentComments(bookStoryId);
    }

    /**
     * 조회된 책 이야기 목록에 대한 좋아요 여부를 확인
     *
     * @param memberId    조회하는 회원의 ID
     * @param bookStories 조회된 책 이야기 목록
     * @return 책 이야기 ID와 좋아요 여부를 매핑한 Map
     */
    public Map<Long, Boolean> checkBookStoryLikeByMemberId(String memberId, List<BookStory> bookStories) {
        if (bookStories == null || bookStories.isEmpty()) {
            return Map.of();
        }

        List<Long> bookStoryIds = bookStories.stream()
                .map(BookStory::getId)
                .distinct()
                .toList();

        if (memberId == null) {
            return bookStoryIds.stream()
                    .collect(Collectors.toMap(
                            bookStoryId -> bookStoryId,
                            bookStoryId -> false
                    ));
        }

        // 배치로 좋아요한 BookStory ID 목록 조회
        List<Long> likedBookStoryIds = bookStoryLikedRepository.findLikedBookStoryIds(memberId,
                bookStoryIds);
        Set<Long> likedIdSet = new HashSet<>(likedBookStoryIds);

        // 모든 BookStory에 대해 좋아요 여부 매핑
        return bookStoryIds.stream()
                .collect(Collectors.toMap(
                        bookStoryId -> bookStoryId,
                        likedIdSet::contains
                ));
    }

    public BookStoryPrevNextProjection retrievePrevNextBookStoryId(String memberId, Long bookStoryId) {
        return bookStoryRepository.findPrevNextBookStoryId(memberId, bookStoryId, BookStoryStatus.PUBLISHED);
    }

    public Comment retrieveBookStoryComment(Long bookStoryCommentId) {
        return commentRepository.findById(bookStoryCommentId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.COMMENT_NOT_FOUND));
    }
}
