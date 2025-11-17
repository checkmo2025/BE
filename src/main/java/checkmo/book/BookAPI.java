package checkmo.book;

import java.util.List;
import java.util.Map;

/**
 * Book Domain Query Facade Book 도메인의 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade
 */
public interface BookAPI {

    /**
     * 다른 도메인에서 사용할 기본적인 책 정보를 조회합니다. (외부용)
     *
     * @param bookId 책 ID (ISBN)
     * @return 공유용 기본 책 정보 DTO
     */
    BookExternalDTO.BasicInfo getBookBasicInfoForShare(String bookId);

    /**
     * 다른 도메인에서 사용할 상세 책 정보를 조회합니다. (외부용)
     *
     * @param bookId 책 ID (ISBN)
     * @return 공유용 상세 책 정보 DTO
     */
    BookExternalDTO.DetailInfo getBookDetailInfoForShare(String bookId);

    /**
     * 책 ID 목록으로 공유용 기본 책 정보를 조회합니다. (외부용)
     *
     * @param bookIds 조회할 책 ID 목록
     * @return 책 ID와 기본 정보 매핑 정보
     */
    Map<String, BookExternalDTO.BasicInfo> getBookBasicInfoMapForShare(List<String> bookIds);

    /**
     * 책을 조회하거나, 존재하지 않으면 생성합니다. (외부용)
     *
     * @param request 생성할 책 정보 DTO
     * @return 조회되거나 생성된 책의 ID (ISBN)
     */
    String getOrCreateBook(BookExternalDTO.BookCreate request);
}