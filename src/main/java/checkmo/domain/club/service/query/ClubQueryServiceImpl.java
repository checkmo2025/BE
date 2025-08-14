package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.category.facade.CategoryQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubMemberRepository;
import checkmo.domain.club.repository.ClubRepository;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryServiceImpl implements ClubQueryService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;

    private final ClubMemberQueryService clubMemberQueryService;
    private final CategoryQueryFacade categoryQueryFacade;

    /**
     * 독서 클럽 목록을 조회합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 검색하기
     *
     * @param keyword 검색 키워드 (모임명 등)
     * @param region 지역 필터 (0: 지역 필터 선택 안함 / 1: 지역 필터 선택해서 검색 키워드로 지역명도 검색 가능)
     * @param participants 지역 필터 (0: 동아리 대상별 검색 필터 선택 안함 / 1: 동아리 대상별 검색 필터 선택해서 검색 키워드로 동아리 대상도 검색 가능)
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return 독서 클럽 목록 DTO
     */
    @Override
    public List<ClubResponseDTO.ClubWithMyStatusDTO> getClubList(String memberId, String keyword, int region, int participants, Long cursorId, Pageable pageable) {

        // 1. 검색 조건에 맞는 클럽 리스트 조회
        int pageSize = pageable.getPageSize();
        List<Club> clubs = clubRepository.searchClubs(keyword, region, participants, cursorId, pageSize);

        // 2. 클럽 ID 리스트 추출
        List<Long> clubIds = clubs.stream()
                .map(Club::getId)
                .toList();

        // 3. 클럽별 멤버 상태 배치 조회
        Map<Long, ClubMember.ClubMemberStatus> statusMap = clubMemberQueryService.getMemberStatuses(memberId, clubIds);

        // 4. 클럽별 카테고리 배치 조회
        Map<Long, List<CategorySharedDTO.CategoryInfo>> categoriesMap = categoryQueryFacade.getCategoriesByClubs(clubIds);

        // 5. DTO 변환 (배치 조회 결과 활용)
        return clubs.stream()
                .map(club -> {
                    ClubMember.ClubMemberStatus status = statusMap.get(club.getId());
                    boolean isStaff = status == ClubMember.ClubMemberStatus.STAFF;
                    boolean isMember = status != null;

                    List<Long> categoryIds = categoriesMap.getOrDefault(club.getId(), List.of())
                            .stream()
                            .map(CategorySharedDTO.CategoryInfo::getId)
                            .toList();

                    ClubResponseDTO.ClubDetailDTO clubDetailDTO = ClubConverter.fromClubToClubDetailDTO(club, categoryIds, isStaff);

                    return ClubResponseDTO.ClubWithMyStatusDTO.builder()
                            .club(clubDetailDTO)
                            .isMember(isMember)
                            .build();
                })
                .toList();
    }

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId) {
        return null;
    }

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId, int size) {
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
        boolean isStaff = clubMember.isStaff();
        if (!isStaff) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 카테고리 DTO
        CategorySharedDTO.CategoryInfoList categoryInfoList = categoryQueryFacade.getCategoriesByClubForShare(clubId);

        List<Long> categoryIds = categoryInfoList.getCategoryList().stream()
                .map(CategorySharedDTO.CategoryInfo::getId)
                .collect(Collectors.toList());

        // 4. Club 엔티티 + 카테고리 ID 리스트 → DTO 변환
        return ClubConverter.fromClubToClubDetailDTO(club, categoryIds, isStaff);

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
