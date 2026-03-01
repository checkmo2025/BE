package checkmo.book;

import java.util.List;
import java.util.Map;

public interface BookAPI {

    /**
     * 기본 책 정보를 조회합니다.
     *
     * @param bookId 책 ID (ISBN)
     * @return 공유용 기본 책 정보 DTO
     */
    BookExternalDTO.BasicInfo fetchBookBasicInfo(String bookId);

    /**
     * 상세 책 정보를 조회합니다.
     *
     * @param bookId 책 ID (ISBN)
     * @return 공유용 상세 책 정보 DTO
     */
    BookExternalDTO.DetailInfo fetchBookDetailInfo(String bookId);

    /**
     * 책 ID 목록으로 기본 책 정보 목록을 조회합니다.
     *
     * @param bookIds 조회할 책 ID 목록
     * @return 책 ID와 기본 정보 매핑 정보
     */
    Map<String, BookExternalDTO.BasicInfo> fetchBookBasicInfoByBookIds(List<String> bookIds);

    /**
     * ISBN으로 책을 조회하거나, 존재하지 않으면 외부 API를 통해 생성합니다.
     *
     * @param isbn 책 ISBN
     * @return 조회되거나 생성된 책의 ID (ISBN)
     */
    String fetchOrCreateBook(String isbn);
}
