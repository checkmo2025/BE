package checkmo.domain.book.facade;

import checkmo.domain.book.converter.BookConverter;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.repository.BookRepository;
import checkmo.domain.book.service.query.AladinApiService;
import checkmo.domain.book.service.query.BookQueryService;
import checkmo.domain.book.web.dto.BookResponseDTO;
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

    private final AladinApiService aladinApiService;
    private final BookQueryService bookQueryService;
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
    public BookSharedDTO.BasicInfoDTO getBookBasicInfoForShare(String bookId) {
        // Service에서 엔티티 받아서 직접 변환
        Book book = bookQueryService.findBook(bookId);
        
        return BookConverter.fromBookToBasicInfoDTO(book);
    }

    @Override
    public BookSharedDTO.DetailInfoDTO getBookDetailInfoForShare(String bookId) {
        // Service에서 엔티티 받아서 직접 변환
        Book book = bookQueryService.findBook(bookId);
        
        return BookConverter.fromBookToDetailInfoDTO(book);
    }

    @Override
    public Map<String, BookSharedDTO.BasicInfoDTO> getBookBasicInfoMapForShare(List<String> bookIds) {
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
