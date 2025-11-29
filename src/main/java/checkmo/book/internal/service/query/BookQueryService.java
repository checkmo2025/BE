package checkmo.book.internal.service.query;

import checkmo.book.internal.entity.Book;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.repository.BookRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class BookQueryService {

    private final BookRepository bookRepository;

    public Book retrieveBook(String bookId) {
        return bookRepository.findById(bookId).orElseThrow(
                () -> new BookException(BookErrorStatus.BOOK_NOT_FOUND)
        );
    }

    public Map<String, Book> retrieveBookByBookIds(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        List<Book> books = bookRepository.findAllById(bookIds);

        return books.stream()
                .collect(Collectors.toMap(
                        Book::getId,
                        book -> book
                ));
    }
}
