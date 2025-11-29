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
@Transactional
public class BookStoryCommandService {

    private final BookAPI bookAPI;

    private final BookStoryRepository bookStoryRepository;

    /**
     * 책이야기를 작성
     *
     * @param memberId 책이야기를 작성하는 회원의 ID
     * @param request  책이야기 요청 DTO
     */
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreate request) {
        String bookId = bookAPI.fetchOrCreateBook(request.getBookInfo());

        BookStory bookStory = BookStoryConverter.toBookStory(request, memberId, bookId);
        BookStory savedBookStory = bookStoryRepository.save(bookStory);

        return savedBookStory.getId();
    }

    /**
     * 책이야기를 수정
     *
     * @param memberId    수정 요청 회원 ID
     * @param bookStoryId 수정할 책 이야기의 ID
     * @param request     수정할 책 이야기 정보 DTO
     */
    public Long updateBookStory(String memberId, Long bookStoryId, BookStoryRequestDTO.BookStoryUpdate request) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        if (!bookStory.verifyOwner(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_AUTHORIZED);
        }

        return bookStory.updateDescription(request.getDescription());
    }

    /**
     * 책이야기를 삭제
     *
     * @param bookStoryId 삭제할 책이야기의 ID
     */
    public void deleteBookStory(String memberId, Long bookStoryId) {
        BookStory bookStory = bookStoryRepository.findById(bookStoryId)
                .orElseThrow(() -> new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_FOUND));

        if (!bookStory.verifyOwner(memberId)) {
            throw new BookStoryException(BookStoryErrorStatus.BOOK_STORY_NOT_AUTHORIZED);
        }

        bookStoryRepository.delete(bookStory);
    }
}
