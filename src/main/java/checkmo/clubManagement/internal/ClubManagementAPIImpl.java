package checkmo.clubManagement.internal;

import checkmo.book.BookAPI;
import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.BookRecommend;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubCategory;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.service.query.ClubBookRecommendQueryService;
import checkmo.clubManagement.internal.service.query.ClubCategoryQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubManagementAPIImpl implements ClubManagementAPI {

    // 페이징 기본 크기 상수 정의
    private static final int DEFAULT_PAGE_SIZE = 10;

    // Domain level 2
    private final MemberAPI memberAPI;

    // Domain level 1
    private final BookAPI bookAPI;

    // 자신의 Query Service
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubCategoryQueryService clubCategoryQueryService;
    private final ClubBookRecommendQueryService clubBookRecommendQueryService;

    @Override
    public ClubResponseDTO.MyClubListDTO getMyClubList(String memberId) {

        // 1. 회원이 가입한 모임 목록 조회
        List<ClubManagementExternalDTO.MyClubInfo> myClubs = clubMemberQueryService.getMyClubList(memberId)
                .getClubList();

        // 2. 모임 정보 DTO로 변환
        List<ClubResponseDTO.ClubInfoDTO> clubInfoDTOList = myClubs.stream()
                .map(ClubManagementConverter::toClubInfoDTOFromMyClubInfo)
                .toList();

        // 3. 최종 DTO 반환
        return ClubResponseDTO.MyClubListDTO.builder()
                .clubList(clubInfoDTOList)
                .build();
    }

    @Override
    public ClubResponseDTO.MyPageClubListDTO getMyPageClubList(String memberId, Long cursorId, Integer size) {

        // 1. 기본 사이즈 처리
        if (size == null) {
            size = DEFAULT_PAGE_SIZE;
        }

        // 2. 서비스 호출 (size+1로 조회 → hasNext 판단)
        List<ClubMember> clubMembers = clubMemberQueryService.getMyPageClubList(memberId, cursorId, size + 1);

        // 3. 페이징 처리
        boolean hasNext = clubMembers.size() > size;
        if (hasNext) {
            clubMembers = clubMembers.subList(0, size);
        }
        Long nextCursor = hasNext ? clubMembers.get(clubMembers.size() - 1).getId() : null;

        // 4. 클럽 ID 수집
        List<Long> clubIds = clubMembers.stream()
                .map(cm -> cm.getClub().getId())
                .toList();

        // 5. 카테고리 배치 조회
        Map<Long, List<String>> clubCategoryNamesMap =
                ClubManagementConverter.fromClubCategoriesToCategoryNamesMap(
                        clubCategoryQueryService.findCategoriesByClubIds(clubIds)
                );

        // 6. DTO 변환
        List<ClubResponseDTO.ClubDetailResponseDTO> dtoList = clubMembers.stream()
                .map(cm -> ClubManagementConverter.fromClubToResponseDTOWithCategoryNames(
                        cm.getClub(),
                        clubCategoryNamesMap.getOrDefault(cm.getClub().getId(), Collections.emptyList()),
                        cm.isStaff()
                ))
                .toList();

        // 7. DTO 감싸서 반환
        return ClubManagementConverter.toMyPageClubListDTO(dtoList, hasNext, nextCursor);
    }

    @Override
    public ClubManagementExternalDTO.MyClubList getMyClubListForShare(String memberId) {
        return clubMemberQueryService.getMyClubList(memberId);
    }

    @Override
    public ClubResponseDTO.ClubListDTO getClubList(String memberId, ClubRequestDTO.ClubSearchFilter filter,
                                                   ClubRequestDTO.CursorPageRequest pageRequest) {

        // 1. 커서 초기화
        Long cursorId = (pageRequest.cursorId() == null || pageRequest.cursorId() == 0L) ? Long.MAX_VALUE
                : pageRequest.cursorId();

        // 2. 페이지 크기 결정 (size가 null 또는 0 이하이면 기본값 사용)
        int pageSize = (pageRequest.size() == null || pageRequest.size() <= 0) ? DEFAULT_PAGE_SIZE : pageRequest.size();

        // 3. Service에서 순수 엔티티 조회
        List<Club> clubs = clubQueryService.getClubList(filter, cursorId, pageSize);

        // 4. 클럽 ID 리스트 추출
        List<Long> clubIds = clubs.stream()
                .map(Club::getId)
                .toList();

        // 5. 클럽별 멤버 상태 배치 조회
        Map<Long, ClubMember.ClubMemberStatus> statusMap = clubMemberQueryService.getMemberStatuses(memberId, clubIds);

        // 6. 클럽별 카테고리 ID 배치 조회
        List<ClubCategory> allClubCategories = clubCategoryQueryService.findCategoriesByClubIds(clubIds);
        Map<Long, List<Long>> categoryIdMap = ClubManagementConverter.fromClubCategoriesToCategoryIdMap(
                allClubCategories);

        // 7. DTO 변환
        List<ClubResponseDTO.ClubWithMyStatusDTO> clubList = clubs.stream()
                .map(club -> toClubWithMyStatusDTO(club, statusMap, categoryIdMap))
                .toList();

        // 8. 페이징 처리 (마지막 ID를 기반으로 다음 페이지 존재 여부 확인)
        Long lastId = clubs.isEmpty() ? null : clubs.getLast().getId();
        boolean hasNext = !clubs.isEmpty() && clubs.size() == pageSize;

        // 9. 최종 DTO 변환
        return ClubManagementConverter.toClubListDTO(clubList, hasNext, lastId);
    }

    /**
     * Club 엔티티를 ClubWithMyStatusDTO로 변환합니다.
     *
     * @param club          클럽 엔티티
     * @param statusMap     클럽별 멤버 상태 맵
     * @param categoryIdMap 클럽별 카테고리 ID 맵
     * @return ClubWithMyStatusDTO
     */
    private ClubResponseDTO.ClubWithMyStatusDTO toClubWithMyStatusDTO(
            Club club,
            Map<Long, ClubMember.ClubMemberStatus> statusMap,
            Map<Long, List<Long>> categoryIdMap
    ) {
        ClubMember.ClubMemberStatus status = statusMap.get(club.getId());
        boolean isStaff = status == ClubMember.ClubMemberStatus.STAFF;
        boolean isMember = status != null;

        List<Long> categoryIds = categoryIdMap.getOrDefault(club.getId(), List.of());

        ClubResponseDTO.ClubDetailDTO clubDetailDTO = ClubManagementConverter.fromClubToClubDetailDTO(club, categoryIds,
                isStaff);

        return ClubResponseDTO.ClubWithMyStatusDTO.builder()
                .club(clubDetailDTO)
                .isMember(isMember)
                .build();
    }

    @Override
    public ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId) {

        // 1. Service에서 순수 엔티티 조회
        Club club = clubQueryService.getClubInfo(clubId);

        // 2. 운영진 권한 확인
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        boolean isStaff = clubMember.isStaff();
        if (!isStaff) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 카테고리 ID 리스트 조회
        List<ClubCategory> clubCategories = clubCategoryQueryService.findCategoriesByClub(clubId);
        List<Long> categoryIds = clubCategories.stream()
                .map(ClubCategory::getCategoryId)
                .toList();

        // 4. DTO 변환 후 반환
        return ClubManagementConverter.fromClubToClubDetailDTO(club, categoryIds, isStaff);
    }

    @Override
    public ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId,
                                                                       String clubMemberStatus, Long cursorId,
                                                                       Integer size) {
        // 1. 클럽 멤버 리스트 조회
        clubQueryService.validateClub(clubId);
        ClubMember requester = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!requester.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 만약 size가 null이면 기본값 사용 후 size+1만큼 조회
        if (size == null) {
            size = DEFAULT_PAGE_SIZE;
        }
        List<ClubMember> members = clubMemberQueryService.getClubMemberListByStatus(clubId, clubMemberStatus, cursorId,
                size + 1);

        // 3. 페이징 처리
        boolean hasNext = members.size() > size;
        if (hasNext) {
            members = members.subList(0, size);
        }
        Long nextCursor = hasNext ? members.getLast().getId() : null;

        // 4. memberId 추출
        List<String> memberIds = extractMemberIds(members);

        // 5. 기본 정보 배치 조회
        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap = memberAPI.getMemberBasicInfoMapForShare(memberIds);

        // 6. DTO 변환
        List<ClubResponseDTO.ClubMemberDTO> dtoList = members.stream()
                .map(cm -> {
                    MemberExternalDTO.BasicInfo memberInfo = memberInfoMap.get(cm.getMemberId());
                    return ClubManagementConverter.toClubMemberDTO(cm, memberInfo);
                })
                .toList();

        return ClubManagementConverter.toClubMemberListDTO(dtoList, hasNext, nextCursor);
    }

    private List<String> extractMemberIds(List<ClubMember> members) {
        if (members == null) {
            return List.of();
        }
        return members.stream()
                .map(ClubMember::getMemberId)
                .distinct()
                .toList();
    }

    @Override
    public boolean isDuplicateClubName(String clubName) {
        return clubQueryService.isDuplicateClubName(clubName);
    }

    @Override
    public ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId, String memberId) {

        // 1. 클럽 검증
        clubQueryService.validateClub(clubId);

        // 2. 클럽 멤버 검증
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 커서 초기화 (페이징 로직)
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 4. ServiceImpl에서 순수 엔티티 조회
        var bookRecommends = clubBookRecommendQueryService.getRecommendedBooks(clubId, cursor, memberId);

        // 5. 외부 도메인 정보 조합 (Facade에서 처리)
        var currentMemberNickname = memberAPI.getMemberBasicInfoForShare(memberId).getNickname();

        var dtoList = bookRecommends.stream()
                .map(bookRecommend -> {
                    var bookInfo = bookAPI.getBookBasicInfoForShare(bookRecommend.getBookId());
                    var authorInfo = memberAPI.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());
                    return ClubManagementConverter.toBookRecommendDetailDTO(bookRecommend, bookInfo, authorInfo,
                            currentMemberNickname, clubMember.isStaff());
                }).toList();

        // 6. 페이징 처리 (Facade에서)
        Long lastId = bookRecommends.isEmpty() ? null : bookRecommends.get(bookRecommends.size() - 1).getId();
        boolean hasNext = clubBookRecommendQueryService.hasNextPage(clubId, lastId);

        return ClubManagementConverter.toBookRecommendListDTO(dtoList, hasNext, lastId);
    }

    @Override
    public ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(Long clubId, Long bookRecommendId,
                                                                           String memberId) {
        // 1. Service에서 순수 엔티티 조회
        BookRecommend bookRecommend = clubBookRecommendQueryService.getBookRecommendEntity(clubId, bookRecommendId,
                memberId);

        // 2. ClubMember 조회 (isStaff 확인용)
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 3. 외부 도메인 정보 조회 (Facade에서 처리)
        var bookInfo = bookAPI.getBookBasicInfoForShare(bookRecommend.getBookId());
        var authorInfo = memberAPI.getMemberBasicInfoForShare(bookRecommend.getClubMember().getMemberId());
        var currentMemberInfo = memberAPI.getMemberBasicInfoForShare(memberId);

        // 4. DTO 변환 후 반환
        return ClubManagementConverter.toBookRecommendDetailDTO(
                bookRecommend,
                bookInfo,
                authorInfo,
                currentMemberInfo.getNickname(),
                clubMember.isStaff()
        );
    }

    @Override
    public Boolean checkStaffStatus(Long clubId, String memberId) {
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        return clubMember.isStaff();
    }

}
