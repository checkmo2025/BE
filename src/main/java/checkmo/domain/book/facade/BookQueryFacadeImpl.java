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
import java.util.stream.Collectors;

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
        var book = bookQueryService.findBook(bookId);

        return BookConverter.fromBookDTOToBasicInfoDTO(book);
    }

    @Override
    public BookSharedDTO.DetailInfoDTO getBookDetailInfoForShare(String bookId) {
        var book = bookQueryService.findBook(bookId);

        return BookConverter.fromBookDTOToDetailInfoDTO(book);
    }

    @Override
    public Map<String, BookSharedDTO.BasicInfoDTO> getBookBasicInfoMapForShare(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        List<String> distinctBookIds = bookIds.stream().distinct().toList();
        
        // 배치로 책 정보 조회
        Map<String, BookResponseDTO.BookInfoDetailResponse> booksMap = bookQueryService.findBooksMap(distinctBookIds);
        
        // DTO 변환
        return booksMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> BookConverter.fromBookDTOToBasicInfoDTO(entry.getValue())
                ));
    }

    @Override
    public Book findBookReferenceById(String bookId) {
        return bookRepository.getReferenceById(bookId);
    }
}
