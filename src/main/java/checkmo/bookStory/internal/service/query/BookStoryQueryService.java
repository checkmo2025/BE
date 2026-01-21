package checkmo.bookStory.internal.service.query;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.repository.BookStoryLikedRepository;
import checkmo.bookStory.internal.repository.BookStoryRepository;
import checkmo.bookStory.internal.repository.CommentRepository;
import checkmo.bookStory.internal.repository.projection.BookStoryPrevNextProjection;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            String targetMemberId,
            Long cursorId,
            int pageSize
    ) {
        return bookStoryRepository.searchBookStories(memberId, scope, clubId, targetMemberId, cursorId, pageSize);
    }

    /**
     * 책 이야기 엔티티 조회
     *
     * @param bookStoryId 조회할 책 이야기의 ID
     * @return 조회된 책 이야기 엔티티
     */
    public BookStory retrieveBookStory(Long bookStoryId) {
        return bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));
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
        return bookStoryRepository.findPrevNextBookStoryId(memberId, bookStoryId);
    }
}
