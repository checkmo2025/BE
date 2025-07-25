package checkmo.domain.club.service.query;

import checkmo.domain.club.entity.BookRecommend;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;

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
     * 독서모임의 추천 책 상세 정보를 조회합니다.
     *
     * 피그마 참고 페이지 : #검색하기 - 첫화면, 검색시
     *
     * @param clubId 독서모임 ID
     * @param bookRecommendId 추천 책 ID
     * @return 추천 책 상세 정보 DTO
     */
    ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(Long clubId, String memberId, Long bookRecommendId);
}
