package checkmo.domain.book.service.command;

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
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(String bookId) {
        bookRepository.deleteById(bookId);
    }
}
