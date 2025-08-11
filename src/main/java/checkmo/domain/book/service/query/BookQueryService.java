package checkmo.domain.book.service.query;

import checkmo.domain.book.web.dto.BookResponseDTO;

import java.util.List;
import java.util.Map;

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

    /**
     * 책 ID 목록으로 배치 조회 (배치 처리용)
     *
     * @param bookIds 조회할 책 ID 목록
     * @return 책 ID와 책 정보 매핑
     */
    Map<String, BookResponseDTO.BookInfoDetailResponse> findBooksMap(List<String> bookIds);
}
