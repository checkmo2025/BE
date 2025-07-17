package checkmo.domain.book.facade;

import checkmo.domain.book.service.query.AladinApiService;
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

    @Override
    public BookResponseDTO.BookInfoDetailResponseDTO findBook(String bookId) {
        return null;
    }

    @Override
    public BookResponseDTO.BookListResponseDTO getBookInfoFromAladin(String keyword, int page) {
        return aladinApiService.getBookInfoFromAladin(keyword, page);
    }

    @Override
    public BookSharedDTO.BasicInfoDTO getBookBasicInfoForShare(String bookId) {
        return null;
    }

    @Override
    public BookSharedDTO.DetailInfoDTO getBookDetailInfoForShare(String bookId) {
        return null;
    }
}
