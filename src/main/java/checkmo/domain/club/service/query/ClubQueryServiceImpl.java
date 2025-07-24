package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.category.facade.CategoryQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryServiceImpl implements ClubQueryService {

    private final ClubRepository clubRepository;
    private final ClubMemberQueryService clubMemberQueryService;
    private final CategoryQueryFacade categoryQueryFacade;

    @Override
    public ClubResponseDTO.ClubListDTO getClubList(String keyword, int region, int participants, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId) {
        return null;
    }

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId, int size) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId) {
        return null;
    }

    /**
     * 독서모임의 상세 정보를 조회합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 검색하기 - 특정 모임 클릭시
     *
     * @param clubId 독서모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @return 독서 클럽 상세 정보 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId) {

        // 1. 클럽 유효성 검증
        Club club = validateClub(clubId);

        // 2. 운영진 권한 확인
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 카테고리 DTO
        CategorySharedDTO.CategoryInfoListDTO categoryInfoListDTO = categoryQueryFacade.getCategoriesByClubForShare(clubId);

        List<Long> categoryIds = categoryInfoListDTO.getCategoryList().stream()
                .map(CategorySharedDTO.CategoryInfoDTO::getId)
                .collect(Collectors.toList());

        // 4. Club 엔티티 + 카테고리 ID 리스트 → DTO 변환
        return ClubConverter.fromClubToClubDetailDTO(club, categoryIds);

    }

    /**
     * 독서모임의 이름 중복 여부를 확인 합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 생성하기 첫화면 첫스크롤
     *
     * @param clubName 독서모임 이름
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    @Override
    public boolean isDuplicateClubName(String clubName) {
        return clubRepository.existsByName(clubName);
    }

    @Override
    public ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, int size) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId) {
        return null;
    }

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId) {
        return null;
    }

    @Override
    public Club validateClub(Long clubId) throws GeneralException {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_NOT_FOUND));
    }
}
