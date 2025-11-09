package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.internal.entity.BookRecommend;
import java.util.List;

public interface ClubBookRecommendQueryService {

    /**
     * 순수하게 BookRecommend 엔티티들만 조회 (페이징 없음)
     *
     * @param clubId   독서모임 ID
     * @param cursorId 커서 ID (페이징용, 처음 조회 시 null 또는 0)
     * @param memberId 회원 ID
     * @return 추천 책 목록 리스트
     */
    List<BookRecommend> getRecommendedBooks(Long clubId, Long cursorId, String memberId);

    boolean hasNextPage(Long clubId, Long lastId);

    /**
     * 독서모임의 추천 책 엔티티를 조회합니다.
     *
     * @param clubId          독서모임 ID
     * @param bookRecommendId 추천 책 ID
     * @param memberId        요청한 회원 ID
     * @return BookRecommend
     */
    BookRecommend getBookRecommendEntity(Long clubId, Long bookRecommendId, String memberId);
}
