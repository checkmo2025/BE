package checkmo.bookStory.service.query;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.bookStory.entity.BookStory;
import checkmo.bookStory.entity.Comment;
import checkmo.bookStory.repository.BookStoryLikedRepository;
import checkmo.bookStory.repository.BookStoryRepository;
import checkmo.bookStory.repository.CommentRepository;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class BookStoryQueryServiceImpl implements BookStoryQueryService {

    // 자신의 Repository
    private final BookStoryRepository bookStoryRepository;
    private final BookStoryLikedRepository bookStoryLikedRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<BookStory> findBookStories(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, String targetMemberId, Long cursorId, int pageSize) {
        // TODO: 현재 내부에서 외부 도메인의 Q클래스를 호출해서 QueryDSL 사용하고 있는데, 이 부분도 리팩토링 필요
        return bookStoryRepository.searchBookStories(memberId, scope, clubId, targetMemberId, cursorId, pageSize + 1);
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
        List<Long> likedBookStoryIds = bookStoryLikedRepository.findLikedBookStoryIdsByMemberIdAndBookStoryIds(memberId, bookStoryIds);
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
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOK_STORY_NOT_FOUND));
    }

    @Override
    public List<Comment> findCommentsByBookStoryId(Long bookStoryId) {
        return commentRepository.findParentComments(bookStoryId);
    }
}
