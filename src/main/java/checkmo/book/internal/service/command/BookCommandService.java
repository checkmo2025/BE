package checkmo.book.internal.service.command;

import checkmo.book.BookExternalDTO;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookRepository;
import checkmo.book.internal.service.query.AladinApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class BookCommandService {

    private final BookRepository bookRepository;
    private final AladinApiService aladinApiService;

    public String saveBook(BookExternalDTO.BookCreate request) {
        if (bookRepository.existsById(request.getIsbn())) {
            return request.getIsbn();
        }

        try {
            Book book = BookConverter.toBook(request);
            return bookRepository.save(book).getId();
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 같은 ISBN이 먼저 생성된 경우
            return request.getIsbn();
        }
    }

    public String fetchOrCreateBook(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new BookException(BookErrorStatus.BOOK_INVALID_REQUEST);
        }

        if (bookRepository.existsById(isbn)) {
            return isbn;
        }

        var detail = aladinApiService.retrieveBookDetailInfo(isbn);
        BookExternalDTO.BookCreate request = BookConverter.toBookCreate(detail);
        return saveBook(request);
    }
}
