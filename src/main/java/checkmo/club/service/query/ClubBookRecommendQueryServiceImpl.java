package checkmo.club.service.query;

import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.club.entity.BookRecommend;
import checkmo.club.repository.BookRecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubBookRecommendQueryServiceImpl implements ClubBookRecommendQueryService {

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 Repository
    private final BookRecommendRepository bookRecommendRepository;


    /**
     * 순수하게 BookRecommend 엔티티들만 조회 (페이징 없음)
     *
     * @param clubId   독서모임 ID
     * @param cursorId 커서 ID (페이징용, 처음 조회 시 null 또는 0)
     * @param memberId 회원 ID
     * @return 추천 책 목록 리스트
     */
    @Override
    public List<BookRecommend> getRecommendedBooks(Long clubId, Long cursorId, String memberId) {

        // 1. 클럽 존재 여부 검증
        clubQueryService.validateClub(clubId);

        // 2. 클럽 멤버 여부 검증
        clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 커서 기반 엔티티 조회만
        return bookRecommendRepository.findTop10ByClubMember_Club_IdAndIdLessThanOrderByIdDesc(clubId, cursorId);
    }

    @Override
    public boolean hasNextPage(Long clubId, Long lastId) {
        if (lastId == null) return false;
        return bookRecommendRepository.existsByClubMember_Club_IdAndIdLessThan(clubId, lastId);
    }

    /**
     * 독서모임의 추천 책 엔티티를 조회합니다.
     *
     * @param clubId 독서모임 ID
     * @param bookRecommendId 추천 책 ID
     * @param memberId 요청한 회원 ID
     * @return BookRecommend 엔티티
     */
    @Override
    public BookRecommend getBookRecommendEntity(Long clubId, Long bookRecommendId, String memberId) {
        // 1. 클럽 검증
        clubQueryService.validateClub(clubId);

        // 2. 클럽 멤버 검증
        clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 추천 책 엔티티 조회 및 반환
        return bookRecommendRepository.findById(bookRecommendId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_BOOK_RECOMMEND_NOT_FOUND));
    }

}