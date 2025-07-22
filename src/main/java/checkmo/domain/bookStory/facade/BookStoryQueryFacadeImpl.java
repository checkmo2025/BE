package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.service.query.BookStoryQueryService;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;
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
    public BookStoryResponseDTO.BookStoryResponse getBookStory(Long bookStoryId) {
        return null;
    }

    @Override
    public BookStoryResponseDTO.BookStoryListResponse getMyBookStories(String memberId, String targetMemberId, Long cursorId) {
        return null;
    }

    @Override
    public BookStoryResponseDTO.BookStoryListResponse getBookStoriesByScope(String memberId, BookStoryRequestDTO.BookStoryScope scope, Long clubId, Long cursorId) {
        return bookStoryQueryService.getBookStoriesByScope(memberId, scope, clubId, cursorId);
    }

    @Override
    public BookStorySharedDTO.BookStoryPreviewListDTO getBookStoryPreviews(String memberId, String targetMemberId, int size) {
        return null;
    }
}
