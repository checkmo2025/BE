package checkmo.domain.club.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.BookRecommend;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.BookRecommendRepository;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.member.facade.MemberQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubBookRecommendQueryServiceImpl implements ClubBookRecommendQueryService {

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;
    // Domain level 1
    private final BookQueryFacade bookQueryFacade;

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
     * 독서모임의 추천 책 상세 정보를 조회합니다.
     *
     * 피그마 참고 페이지 : #검색하기 - 첫화면, 검색시
     *
     * @param clubId 독서모임 ID
     * @param bookRecommendId 추천 책 ID
     * @return 추천 책 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(
            Long clubId,
            String memberId,
            Long bookRecommendId
    ) {
        // 1. 클럽 검증
        clubQueryService.validateClub(clubId);

        // 2. 클럽 멤버 검증
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 추천 책 엔티티 조회
        BookRecommend bookRecommend = bookRecommendRepository.findById(bookRecommendId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_BOOK_RECOMMEND_NOT_FOUND));

        // 4. 책 및 작성자 정보 (프록시로 id 조회)
        var bookInfo = bookQueryFacade.getBookBasicInfoForShare(bookRecommend.getBookId());
        var authorInfo = memberQueryFacade.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());

        // 5. 현재 사용자 정보 조회 → 닉네임
        var currentMemberInfo = memberQueryFacade.getMemberBasicInfoForShare(memberId);
        var currentNickname = currentMemberInfo.getNickname();

        // 6. DTO 변환 후 반환
        return ClubConverter.toBookRecommendDetailDTO(bookRecommend, bookInfo, authorInfo, currentNickname, clubMember.isStaff());
    }

}