package checkmo.book.facade;

import checkmo.book.converter.BookConverter;
import checkmo.book.entity.Book;
import checkmo.book.repository.BookRepository;
import checkmo.book.service.query.AladinApiService;
import checkmo.book.service.query.BookQueryService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.global.dto.BookSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookQueryFacadeImpl implements BookQueryFacade {

    // 외부 API 서비스
    private final AladinApiService aladinApiService;

    // 자신의 QueryService
    private final BookQueryService bookQueryService;

    // 자신의 Repository
    private final BookRepository bookRepository;

    @Override
    public BookResponseDTO.BookInfoDetailResponse getBookDetailFromAladin(String bookId) {
        return aladinApiService.getBookDetailInfoFromAladin(bookId);
    }

    @Override
    public BookResponseDTO.BookListResponse searchBookFromAladin(String keyword, int page) {
        return aladinApiService.searchBookFromAladin(keyword, page);
    }

    @Override
    public BookSharedDTO.BasicInfo getBookBasicInfoForShare(String bookId) {
        // Service에서 엔티티 받아서 직접 변환
        Book book = bookQueryService.findBook(bookId);
        
        return BookConverter.fromBookToBasicInfoDTO(book);
    }

    @Override
    public BookSharedDTO.DetailInfo getBookDetailInfoForShare(String bookId) {
        // Service에서 엔티티 받아서 직접 변환
        Book book = bookQueryService.findBook(bookId);
        
        return BookConverter.fromBookToDetailInfoDTO(book);
    }

    @Override
    public Map<String, BookSharedDTO.BasicInfo> getBookBasicInfoMapForShare(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        List<String> distinctBookIds = bookIds.stream().distinct().toList();
        
        // 배치로 책 엔티티 조회
        Map<String, Book> booksMap = bookQueryService.findBooksMap(distinctBookIds);
        
        // 엔티티 → SharedDTO 직접 변환
        return BookConverter.fromBooksMapToBasicInfoDTOMap(booksMap);
    }

    @Override
    public Book findBookReferenceById(String bookId) {
        return bookRepository.getReferenceById(bookId);
    }
}
