package checkmo.domain.book.service.query;

import checkmo.domain.book.web.dto.BookResponseDTO;

public interface AladinApiService {

    /**
     * 알라딘 API를 통해 책을 검색하는 메서드
     *
     * @param keyword 책 제목 + 저자명
     * @param page 페이지 번호
     * @return 책 정보 List DTO
     */
    BookResponseDTO.BookListResponse searchBookFromAladin(String keyword, int page);

    /**
     * 알라딘 API를 통해 특정 책을 조회하는 메서드
     *
     * @param isbn 책의 13자리 ISBN
     * @return 책 정보 DTO
     */
    BookResponseDTO.BookInfoDetailResponse getBookDetailInfoFromAladin(String isbn);
}
