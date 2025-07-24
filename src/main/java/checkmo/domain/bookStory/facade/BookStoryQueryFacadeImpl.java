package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.service.query.BookStoryQueryService;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.global.dto.BookStorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryFacadeImpl implements BookStoryQueryFacade {

    private final BookStoryQueryService bookStoryQueryService;

    @Override
    public BookStorySharedDTO.BookStoryResponse getBookStory(String memberId, Long bookStoryId) {
        return bookStoryQueryService.getBookStory(memberId, bookStoryId);
    }

    @Override
    public BookStorySharedDTO.BookStoryListResponse getMyBookStories(String memberId, String targetMemberNickname, Long cursorId) {
        return null;
    }

    @Override
    public BookStorySharedDTO.BookStoryListResponse getBookStoriesByScope(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, Long cursorId) {
        return bookStoryQueryService.getBookStoriesByScope(memberId, scope, clubId, cursorId);
    }
}
