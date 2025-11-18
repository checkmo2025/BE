package checkmo.book.internal;

import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.entity.Book;
import checkmo.book.internal.repository.BookRepository;
import checkmo.book.internal.service.query.BookQueryService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookAPIImpl implements BookAPI {

    // 자신의 QueryService
    private final BookQueryService bookQueryService;

    // 자신의 Repository
    private final BookRepository bookRepository;

    @Override
    public BookExternalDTO.BasicInfo getBookBasicInfoForShare(String bookId) {
        // Service에서 엔티티 받아서 직접 변환
        Book book = bookQueryService.findBook(bookId);

        return BookConverter.toBasicInfoDTO(book);
    }

    @Override
    public BookExternalDTO.DetailInfo getBookDetailInfoForShare(String bookId) {
        // Service에서 엔티티 받아서 직접 변환
        Book book = bookQueryService.findBook(bookId);

        return BookConverter.toDetailInfoDTO(book);
    }

    @Override
    public Map<String, BookExternalDTO.BasicInfo> getBookBasicInfoMapForShare(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        List<String> distinctBookIds = bookIds.stream().distinct().toList();

        // 배치로 책 엔티티 조회
        Map<String, Book> booksMap = bookQueryService.findBooksMap(distinctBookIds);

        // 엔티티 → SharedDTO 직접 변환
        return BookConverter.toBasicInfoDTOMap(booksMap);
    }

    @Override
    @Transactional
    public String getOrCreateBook(BookExternalDTO.BookCreate request) {

        // 이미 존재하는지 확인
        if (bookRepository.existsById(request.getIsbn())) {
            return request.getIsbn();
        }

        Book book = BookConverter.toBook(request);

        // 없으면 저장 후 ID 반환
        return bookRepository.save(book).getId();
    }
}
