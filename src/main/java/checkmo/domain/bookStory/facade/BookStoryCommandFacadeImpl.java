package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryCommandFacadeImpl implements BookStoryCommandFacade {

    @Override
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequestDTO request) {
        return 0L;
    }

    @Override
    public Long updateBookStory(String memberId, BookStoryRequestDTO.BookStoryUpdateRequestDTO request) {
        return 0L;
    }

    @Override
    public Long deleteBookStory(String memberId, Long bookStoryId) {
        return 0L;
    }

    @Override
    public Long toggleLikeOnBookStory(Long userId, Long bookStoryId) {
        return 0L;
    }
}
