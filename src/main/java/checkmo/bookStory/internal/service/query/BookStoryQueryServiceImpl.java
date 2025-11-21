package checkmo.bookStory.internal.service.query;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.entity.Comment;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.repository.BookStoryLikedRepository;
import checkmo.bookStory.internal.repository.BookStoryRepository;
import checkmo.bookStory.internal.repository.CommentRepository;
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
public class BookStoryQueryServiceImpl implements BookStoryQueryService {

    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<BookStory> findBookStories(
            String memberId,
            BookStoryRequestDTO.BookStoryScope scope,
            Long clubId,
            String targetMemberId,
            Long cursorId,
            int pageSize
    ) {
        return bookStoryRepository.searchBookStories(memberId, scope, clubId, targetMemberId, cursorId, pageSize);
    }

    @Override
    public Map<Long, Boolean> checkLikesForBookStories(String memberId, List<BookStory> bookStories) {
        if (bookStories == null || bookStories.isEmpty()) {
            return Map.of();
        }

        List<Long> bookStoryIds = bookStories.stream()
                .map(BookStory::getId)
                .distinct()
                .toList();

        // 배치로 좋아요한 BookStory ID 목록 조회
        List<Long> likedBookStoryIds = bookStoryLikedRepository.findLikedBookStoryIdsByMemberIdAndBookStoryIds(memberId,
                bookStoryIds);
        Set<Long> likedIdSet = new HashSet<>(likedBookStoryIds);

        // 모든 BookStory에 대해 좋아요 여부 매핑
        return bookStoryIds.stream()
                .collect(Collectors.toMap(
                        bookStoryId -> bookStoryId,
                        likedIdSet::contains
                ));
    }


    @Override
    public BookStory findBookStoryById(Long bookStoryId) {
        return bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));
    }

    @Override
    public List<Comment> findCommentsByBookStoryId(Long bookStoryId) {
        return commentRepository.findParentComments(bookStoryId);
    }
}
