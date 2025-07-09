package checkmo.domain.book.service.query;

import checkmo.domain.book.dto.BookResponseDTO;

import java.util.List;

public interface AladinApiService {
    /**
     * 알라딘 API를 통해 책 정보를 가져오는 메서드
     *
     * @param keyword 책 제목 + 저자명
     * @return 책 정보 List DTO
     */
    List<BookResponseDTO.BookInfoDetailResponseDTO> getBookInfoFromAladin(String keyword);
}
