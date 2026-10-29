package checkmo.book.internal.service.command;

import checkmo.book.BookExternalDTO;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookRepository;
import checkmo.book.internal.service.query.Yes24ApiService;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class BookCommandService {

    private final BookRepository bookRepository;
    private final Yes24ApiService yes24ApiService;

    public String fetchOrCreateBook(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new BookException(BookErrorStatus.BOOK_INVALID_REQUEST);
        }

        if (bookRepository.existsById(isbn)) {
            return isbn;
        }

        var detail = yes24ApiService.retrieveBookDetailInfo(isbn);
        if (detail == null || !isbn.equals(detail.getIsbn())) {
            throw new BookException(BookErrorStatus.BOOK_NOT_FOUND);
        }
        BookExternalDTO.BookCreate request = BookConverter.toBookCreate(detail);
        return saveBook(request);
    }

    private String saveBook(BookExternalDTO.BookCreate request) {
        if (bookRepository.existsById(request.getIsbn())) {
            return request.getIsbn();
        }

        Book book = BookConverter.toBook(request);
        // 중복 ISBN만 원자적으로 재사용한다. 길이/무결성 오류는 성공으로 숨기지 않는다.
        bookRepository.insertIfAbsent(book, LocalDateTime.now());
        return book.getId();
    }
}
