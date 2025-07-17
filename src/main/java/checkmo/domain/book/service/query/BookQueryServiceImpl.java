package checkmo.domain.book.service.query;

import checkmo.domain.book.converter.BookConverter;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.repository.BookRepository;
import checkmo.domain.book.web.dto.BookResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
