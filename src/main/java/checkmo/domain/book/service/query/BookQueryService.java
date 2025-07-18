package checkmo.domain.book.service.query;

import checkmo.domain.book.web.dto.BookResponseDTO;

/**
 * 책 관련 모든 조회 기능
 */
public interface BookQueryService {
    /**
     * 책 단건 조회
     *
     * @param bookId 책 ID
     * @return 책 정보 DTO
     */
    BookResponseDTO.BookInfoDetailResponse findBook(String bookId);
}
