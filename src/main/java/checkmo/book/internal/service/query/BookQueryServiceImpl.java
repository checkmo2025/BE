package checkmo.book.internal.service.query;

import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookQueryServiceImpl implements BookQueryService {

    // 자신의 Repository
    private final BookRepository bookRepository;

    @Override
    public Book findBook(String bookId) {
        return bookRepository.findById(bookId).orElseThrow(
                () -> new IllegalArgumentException("책을 찾을 수 없습니다. bookId: " + bookId)
        );
    }

    @Override
    public Map<String, Book> findBooksMap(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        // 배치로 책 엔티티 조회하여 Map으로 변환
        List<Book> books = bookRepository.findAllById(bookIds);

        return books.stream()
                .collect(Collectors.toMap(
                        Book::getId,
                        book -> book
                ));
    }
}
