package checkmo.domain.book.facade;

import checkmo.domain.book.web.dto.BookRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCommandFacadeImpl implements BookCommandFacade {

    @Override
    public void saveBookFromAladin(BookRequestDTO.Aladin2BookDTO request) {

    }

    @Override
    public void deleteBook(String bookId) {

    }
}
