package checkmo.domain.bookStory.service.command;

import checkmo.domain.bookStory.web.dto.BookStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryCommandServiceImpl implements BookStoryCommandService {

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
}
