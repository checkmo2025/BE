package checkmo.domain.bookStory.facade;

import checkmo.domain.bookStory.service.command.BookStoryCommandService;
import checkmo.domain.bookStory.service.command.BookStorySocialCommandService;
import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryCommandFacadeImpl implements BookStoryCommandFacade {

    private final BookStoryCommandService bookStoryCommandService;
    private final BookStorySocialCommandService bookStorySocialCommandService;

    @Override
    @Transactional
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequestDTO request) {
        return bookStoryCommandService.createBookStory(memberId, request);
    }

    @Override
    @Transactional
    public Long updateBookStory(String memberId, BookStoryRequestDTO.BookStoryUpdateRequestDTO request) {
        return 0L;
    }

    @Override
    @Transactional
    public Long deleteBookStory(String memberId, Long bookStoryId) {
        return 0L;
    }

    @Override
    @Transactional
    public Long toggleLikeOnBookStory(Long userId, Long bookStoryId) {
        return 0L;
    }
}
