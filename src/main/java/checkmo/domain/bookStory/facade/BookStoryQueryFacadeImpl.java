package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;
import checkmo.global.dto.BookStorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryFacadeImpl implements BookStoryQueryFacade {

    @Override
    public BookStoryResponseDTO.BookStoryResponse getBookStory(Long bookStoryId) {
        return null;
    }

    @Override
    public BookStoryResponseDTO.BookStoryListResponse getMyBookStories(String memberId, Long cursorId) {
        return null;
    }

    @Override
    public BookStorySharedDTO.BookStoryPreviewListDTO getBookStoryPreviews(String memberId, int size) {
        return null;
    }
}
