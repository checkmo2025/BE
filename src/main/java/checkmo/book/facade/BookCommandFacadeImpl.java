package checkmo.book.facade;

import checkmo.book.service.command.BookCommandService;
import checkmo.global.dto.BookSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookCommandFacadeImpl implements BookCommandFacade {

    // 자신의 CommandService
    private final BookCommandService bookCommandService;

    @Override
    @Transactional
    public void saveBook(BookSharedDTO.BookCreateRequest request) {
        bookCommandService.saveBook(request);
    }

    @Override
    @Transactional
    public void deleteBook(String bookId) {
        bookCommandService.deleteBook(bookId);
    }
}
