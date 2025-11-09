package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.entity.BookRecommend;
import checkmo.clubManagement.repository.BookRecommendRepository;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubBookRecommendQueryServiceImpl implements ClubBookRecommendQueryService {

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 Repository
    private final BookRecommendRepository bookRecommendRepository;

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
        if (lastId == null) {
            return false;
        }
        return bookRecommendRepository.existsByClubMember_Club_IdAndIdLessThan(clubId, lastId);
    }

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