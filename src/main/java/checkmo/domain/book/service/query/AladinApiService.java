package checkmo.domain.book.service.query;

import checkmo.domain.book.web.dto.BookResponseDTO;

public interface AladinApiService {
    /**
     * 알라딘 API를 통해 책 정보를 가져오는 메서드
     *
     * @param keyword 책 제목 + 저자명
     * @param page 페이지 번호
     * @return 책 정보 List DTO
     */
    BookResponseDTO.BookListResponseDTO getBookInfoFromAladin(String keyword, int page);
}
