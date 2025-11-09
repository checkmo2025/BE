package checkmo.book.internal.service.command;

import checkmo.book.BookSharedDTO;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookRepository;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCommandServiceImpl implements BookCommandService {

    // 자신의 Repository
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void saveBook(BookSharedDTO.BookCreateRequest request) {
        Book book = BookConverter.fromBookCreateRequest(request);

        // 이미 존재하는 책인지 확인
        if (bookRepository.existsById(book.getId())) {
            return; // 이미 존재하면 저장하지 않음
        }

        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(String bookId) {

        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new GeneralException(ErrorStatus.BOOK_NOT_FOUND)
        );

        bookRepository.delete(book);
    }
}
