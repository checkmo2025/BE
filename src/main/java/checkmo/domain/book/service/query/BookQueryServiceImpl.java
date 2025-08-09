package checkmo.domain.book.service.query;

import checkmo.domain.book.converter.BookConverter;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.repository.BookRepository;
import checkmo.domain.book.web.dto.BookResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookQueryServiceImpl implements BookQueryService {

    private final BookRepository bookRepository;

    @Override
    public BookResponseDTO.BookInfoDetailResponse findBook(String bookId) {

        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new IllegalArgumentException("책을 찾을 수 없습니다. bookId: " + bookId)
        );

        return BookConverter.fromBook(book);
    }

    @Override
    public Map<String, BookResponseDTO.BookInfoDetailResponse> findBooksMap(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        // 배치로 책 엔티티 조회
        List<Book> books = bookRepository.findAllById(bookIds);

        // Book 엔티티를 DTO로 변환하여 매핑
        return books.stream()
                .collect(Collectors.toMap(
                        Book::getId,
                        BookConverter::fromBook
                ));
    }
}
