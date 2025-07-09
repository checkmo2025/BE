package checkmo.domain.book.service.query;

import checkmo.domain.book.dto.BookResponseDTO;

import java.util.List;

/**
 * 책 관련 모든 조회 기능
 */
public interface BookQueryService {
    /**
     * 책의 목록 조회
     *
     * @param bookId 책 ID
     * @return 책 목록
     */
    List<BookResponseDTO.BookInfoDetailResponseDTO> findBooksByCategory(List<String> bookId);
}
