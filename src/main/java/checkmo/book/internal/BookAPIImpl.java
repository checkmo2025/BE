package checkmo.book.internal;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.service.command.BookCommandService;
import checkmo.book.internal.service.query.BookQueryService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookAPIImpl implements BookAPI {

    private final BookCommandService bookCommandService;
    private final BookQueryService bookQueryService;

    @Override
    public BookExternalDTO.BasicInfo getBookBasicInfoForShare(String bookId) {
        Book book = bookQueryService.findBook(bookId);

        return BookConverter.toBasicInfoDTO(book);
    }

    @Override
    public BookExternalDTO.DetailInfo getBookDetailInfoForShare(String bookId) {
        Book book = bookQueryService.findBook(bookId);

        return BookConverter.toDetailInfoDTO(book);
    }

    @Override
    public Map<String, BookExternalDTO.BasicInfo> getBookBasicInfoMapForShare(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        List<String> distinctBookIds = bookIds.stream().distinct().toList();

        Map<String, Book> booksMap = bookQueryService.findBooksMap(distinctBookIds);

        return BookConverter.toBasicInfoDTOMap(booksMap);
    }

    @Override
    public String getOrCreateBook(BookExternalDTO.BookCreate request) {
        return bookCommandService.saveBook(request);
    }
}
