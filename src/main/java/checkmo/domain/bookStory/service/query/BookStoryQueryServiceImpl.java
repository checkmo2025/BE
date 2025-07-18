package checkmo.domain.bookStory.service.query;

import checkmo.domain.bookStory.web.dto.BookStoryResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryQueryServiceImpl implements BookStoryQueryService {

    @Override
    public BookStoryResponseDTO.BookStoryResponse getBookStory(Long bookStoryId) {
        return null;
    }

    @Override
    public BookStoryResponseDTO.BookStoryListResponse getMyBookStories(String memberId, Long cursorId) {
        return null;
    }

    @Override
    public BookStoryResponseDTO.BookStoryListResponse getMyBookStories(String memberId, int size) {
        return null;
    }
}
