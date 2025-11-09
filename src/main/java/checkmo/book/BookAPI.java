package checkmo.book;

import checkmo.book.entity.Book;
import checkmo.book.web.dto.BookResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * Book Domain Query Facade
 * Book 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade
 */
public interface BookAPI {

    /**
     * 책의 상세 정보를 조회합니다. (내부용)
     *
     * @param bookId 책 ID (ISBN)
     * @return 책 상세 정보 DTO
     */
    BookResponseDTO.BookInfoDetailResponse getBookDetailFromAladin(String bookId); //

    /**
     * 알라딘 API를 통해 책 정보를 검색합니다. (내부용)
     *
     * @param keyword 검색 키워드 (책 제목, 저자 등)
     * @param page    페이지 번호
     * @return 검색된 책 정보 목록 DTO
     */
    BookResponseDTO.BookListResponse searchBookFromAladin(String keyword, int page); //

    /**
     * 다른 도메인에서 사용할 기본적인 책 정보를 조회합니다. (외부용)
     *
     * @param bookId 책 ID (ISBN)
     * @return 공유용 기본 책 정보 DTO
     */
    BookSharedDTO.BasicInfo getBookBasicInfoForShare(String bookId);

    /**
     * 다른 도메인에서 사용할 상세 책 정보를 조회합니다. (외부용)
     *
     * @param bookId 책 ID (ISBN)
     * @return 공유용 상세 책 정보 DTO
     */
    BookSharedDTO.DetailInfo getBookDetailInfoForShare(String bookId);

    /**
     * 책 ID 목록으로 공유용 기본 책 정보를 조회합니다. (외부용)
     *
     * @param bookIds 조회할 책 ID 목록
     * @return 책 ID와 기본 정보 매핑 정보
     */
    Map<String, BookSharedDTO.BasicInfo> getBookBasicInfoMapForShare(List<String> bookIds);

    /**
     * 다른 도메인에서 관계 설정을 위해 엔티티의 프록시(참조)를 조회합니다. (외부용)
     * ‼️ 이 메소드는 실제 DB 조회를 발생시키지 않는 메소드!!!
     * ‼️ 그리고 반드시 외래 키를 설정하는 용도로만 사용되어야 함!
     *
     * @param bookId 참조할 책의 ID (ISBN)
     * @return Book 엔티티의 프록시 객체
     */
    Book findBookReferenceById(String bookId);

    /**
     * 책을 조회하거나, 존재하지 않으면 생성합니다. (외부용)
     *
     * @param request 생성할 책 정보 DTO
     * @return 조회되거나 생성된 책의 ID (ISBN)
     */
    String getOrCreateBook(BookSharedDTO.BookCreateRequest request);
}