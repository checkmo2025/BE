package checkmo.domain.book.facade;

import checkmo.domain.book.web.dto.BookResponseDTO;
import checkmo.global.dto.BookSharedDTO;

/**
 * Book Domain Query Facade
 * Book 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade
 */
public interface BookQueryFacade {

    /**
     * 책의 상세 정보를 조회합니다. (내부용)
     *
     * @param bookId 책 ID (ISBN)
     * @return 책 상세 정보 DTO
     */
    BookResponseDTO.BookInfoDetailResponseDTO findBook(String bookId); //

    /**
     * 알라딘 API를 통해 책 정보를 검색합니다. (내부용)
     *
     * @param keyword 검색 키워드 (책 제목, 저자 등)
     * @param page    페이지 번호
     * @return 검색된 책 정보 목록 DTO
     */
    BookResponseDTO.BookListResponseDTO getBookInfoFromAladin(String keyword, int page); //

    /**
     * 다른 도메인에서 사용할 기본적인 책 정보를 조회합니다. (외부용)
     *
     * @param bookId 책 ID (ISBN)
     * @return 공유용 기본 책 정보 DTO
     */
    BookSharedDTO.BasicInfoDTO getBookBasicInfoForShare(String bookId);

    /**
     * 다른 도메인에서 사용할 상세 책 정보를 조회합니다. (외부용)
     *
     * @param bookId 책 ID (ISBN)
     * @return 공유용 상세 책 정보 DTO
     */
    BookSharedDTO.DetailInfoDTO getBookDetailInfoForShare(String bookId);
}