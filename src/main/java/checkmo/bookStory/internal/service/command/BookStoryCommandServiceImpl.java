package checkmo.bookStory.internal.service.command;

import checkmo.book.BookAPI;
import checkmo.bookStory.internal.converter.BookStoryConverter;
import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.exception.BookStoryErrorStatus;
import checkmo.bookStory.internal.exception.BookStoryException;
import checkmo.bookStory.internal.repository.BookStoryRepository;
import checkmo.bookStory.web.dto.BookStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookStoryCommandServiceImpl implements BookStoryCommandService {

    // Domain level 1
    private final BookAPI bookAPI;

    private final BookStoryRepository bookStoryRepository;

    @Override
    @Transactional
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreate request) {
        String bookId = bookAPI.fetchOrCreateBook(request.getBookInfo());

        BookStory bookStory = BookStoryConverter.toBookStory(request, memberId, bookId);
        BookStory savedBookStory = bookStoryRepository.save(bookStory);

        return savedBookStory.getId();
    }

    @Override
    @Transactional
    public Long updateBookStory(String memberId, Long bookStoryId, BookStoryRequestDTO.BookStoryUpdate request) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        if (!bookStory.getMemberId().equals(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_AUTHORIZED);
        }

        return bookStory.updateDescription(request.getDescription());
    }

    @Override
    @Transactional
    public void deleteBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        if (!bookStory.getMemberId().equals(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_AUTHORIZED);
        }

        bookStoryRepository.delete(bookStory);
    }
}
