package checkmo.domain.book.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.converter.BookConverter;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.repository.BookRepository;
import checkmo.global.dto.BookSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCommandServiceImpl implements BookCommandService {

    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void saveBook(BookSharedDTO.BookCreateRequestDTO request) {
        Book book = BookConverter.fromBookCreateRequestDTO(request);

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
