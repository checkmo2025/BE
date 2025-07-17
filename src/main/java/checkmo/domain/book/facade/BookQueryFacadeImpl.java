package checkmo.domain.book.facade;

import checkmo.domain.book.converter.BookConverter;
import checkmo.domain.book.service.query.AladinApiService;
import checkmo.domain.book.service.query.BookQueryService;
import checkmo.domain.book.web.dto.BookResponseDTO;
import checkmo.global.dto.BookSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookQueryFacadeImpl implements BookQueryFacade {

    private final AladinApiService aladinApiService;
    private final BookQueryService bookQueryService;

    @Override
    public BookResponseDTO.BookInfoDetailResponseDTO findBook(String bookId) {
        return null;
    }

    @Override
    public BookResponseDTO.BookListResponseDTO searchBookFromAladin(String keyword, int page) {
        return aladinApiService.getBookInfoFromAladin(keyword, page);
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
}
