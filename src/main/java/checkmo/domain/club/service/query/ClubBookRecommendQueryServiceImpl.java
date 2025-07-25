package checkmo.domain.club.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.BookRecommend;
import checkmo.domain.club.repository.BookRecommendRepository;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.member.facade.MemberQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubBookRecommendQueryServiceImpl implements ClubBookRecommendQueryService {

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final BookRecommendRepository bookRecommendRepository;

    private final MemberQueryFacade memberQueryFacade;
    private final BookQueryFacade bookQueryFacade;

    /**
     * 독서모임의 추천 책 목록을 조회합니다.
     *
     * 피그마 참고 페이지 : #검색하기 - 첫화면, 검색시
     *
     * @param clubId 독서모임 ID
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return 추천 책 목록 DTO
     */
    @Override
    public ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId, String memberId) {

        // 1. 클럽 존재 여부 검증
        clubQueryService.validateClub(clubId);

        // 2. 클럽 멤버 여부 검증
        clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 커서 초기화
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 4. 커서 기반 추천 도서 리스트 조회
        var bookRecommends = bookRecommendRepository.findTop10ByClubMember_Club_IdAndIdLessThanOrderByIdDesc(clubId, cursor);

        // 5. 멤버 조회 (작성자 여부)
        var currentMemberNickname = memberQueryFacade.getMemberBasicInfoForShare(memberId).getNickname();

        // 6. dto 변환
        var dtoList = bookRecommends.stream()
                .map(bookRecommend -> {
                    var bookInfo = bookQueryFacade.getBookBasicInfoForShare(bookRecommend.getBookId());
                    var authorInfo = memberQueryFacade.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());
                    return ClubConverter.toBookRecommendDetailDTO(bookRecommend, bookInfo, authorInfo, currentMemberNickname);
                }).toList();

        // 7. 다음 커서 설정
        Long lastId = bookRecommends.isEmpty() ? null : bookRecommends.get(bookRecommends.size() - 1).getId();

        // 8. 다음 페이지 존재 여부 체크
        boolean hasNext = false;
        if (lastId != null) {
            hasNext = bookRecommendRepository.existsByClubMember_Club_IdAndIdLessThan(clubId, lastId);
        }

        // 9. DTO 변환 후 반환
        return ClubConverter.toBookRecommendListDTO(dtoList, hasNext, lastId);
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
        clubMemberQueryService.validateClubMember(clubId, memberId);

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
        return ClubConverter.toBookRecommendDetailDTO(bookRecommend, bookInfo, authorInfo, currentNickname);
    }

}