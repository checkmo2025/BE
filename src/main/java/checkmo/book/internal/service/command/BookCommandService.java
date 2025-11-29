package checkmo.book.internal.service.command;

import checkmo.book.BookExternalDTO;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class BookCommandService {

    private final BookRepository bookRepository;

    public String saveBook(BookExternalDTO.BookCreate request) {
        if (bookRepository.existsById(request.getIsbn())) {
            return request.getIsbn();
        }

        Book book = BookConverter.toBook(request);

        return bookRepository.save(book).getId();
    }
}
