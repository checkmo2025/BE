package checkmo.clubManagement.internal.service;

import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMember.ClubMemberStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO.ClubDetail;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static checkmo.clubManagement.ClubManagementExternalDTO.BasicInfo;

@Service
@RequiredArgsConstructor
public class ClubManagementQueryFacade {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final MemberAPI memberAPI;

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    public ClubResponseDTO.ClubList retrieveClubList(
            String memberId,
            ClubRequestDTO.ClubSearchFilter filter,
            ClubRequestDTO.CursorInfo pageRequest
    ) {
        CursorResult<Club> clubCursorResult = CursorPagingHelper.getPage(
                pageSize -> clubManagementQueryService.retrieveClubs(filter, pageRequest.cursorId(), pageSize),
                Club::getId,
                DEFAULT_PAGE_SIZE
        );

        List<Club> clubs = clubCursorResult.content();
        List<Long> clubIds = extractClubIds(clubs);

        // 클럽별 멤버 상태 배치 조회
        Map<Long, ClubMember.ClubMemberStatus> statusMap = clubMemberQueryService.retrieveClubMemberStatusByClubIds(
                memberId, clubIds);

        List<ClubResponseDTO.ClubWithMyStatus> clubList = clubs.stream()
                .map(club -> toClubWithMyStatusDTO(club, statusMap))
                .toList();

        // 8. 최종 DTO 변환
        return ClubResponseDTO.ClubList.builder()
                .clubList(clubList)
                .hasNext(clubCursorResult.hasNext())
                .nextCursor(clubCursorResult.nextCursor())
                .pageSize(clubList.size())
                .build();
    }

    private ClubResponseDTO.ClubWithMyStatus toClubWithMyStatusDTO(
            Club club,
            Map<Long, ClubMember.ClubMemberStatus> statusMap
    ) {
        ClubMember.ClubMemberStatus status = statusMap.get(club.getId());
        boolean isStaff = (status == ClubMemberStatus.STAFF);
        boolean isMember = (status != null);

        ClubResponseDTO.ClubDetail clubDetail = ClubManagementConverter.toClubDetailDTO(club, isStaff);

        return ClubResponseDTO.ClubWithMyStatus.builder()
                .club(clubDetail)
                .isMember(isMember)
                .build();
    }

    private List<Long> extractClubIds(List<Club> clubs) {
        return clubs.stream()
                .map(Club::getId)
                .toList();
    }

    public ClubResponseDTO.MyClubList retrieveMyClubList(String memberId) {
        // 1. 회원이 가입한 모임 목록 조회
        List<BasicInfo> myClubs = clubMemberQueryService.retrieveClubList(memberId)
                .getClubList();

        // 2. 모임 정보 DTO로 변환
        List<ClubResponseDTO.ClubInfo> clubInfoList = myClubs.stream()
                .map(ClubManagementConverter::toClubInfoDTO)
                .toList();

        // 3. 최종 DTO 반환
        return ClubResponseDTO.MyClubList.builder()
                .clubList(clubInfoList)
                .build();
    }

    public ClubResponseDTO.MyPageClubList retrieveMyPageClubList(String memberId, Long cursorId) {
        CursorResult<ClubMember> clubMemberCursorResult = CursorPagingHelper.getPage(
                pageSize -> clubMemberQueryService.retrieveClubMembers(memberId, cursorId, pageSize),
                ClubMember::getId,
                DEFAULT_PAGE_SIZE
        );
        List<ClubMember> clubMembers = clubMemberCursorResult.content();

        List<ClubResponseDTO.ClubDetail> clubList = convertToClubResponseDTO(clubMembers);
        return ClubResponseDTO.MyPageClubList.builder()
                .clubList(clubList)
                .hasNext(clubMemberCursorResult.hasNext())
                .nextCursor(clubMemberCursorResult.nextCursor())
                .build();
    }

    private List<ClubDetail> convertToClubResponseDTO(List<ClubMember> clubMembers) {
        return clubMembers.stream()
                .map(cm -> ClubManagementConverter.toClubDetailDTO(
                        cm.getClub(),
                        cm.isStaff()
                ))
                .toList();
    }

    public ClubResponseDTO.ClubDetail retrieveClubDetail(Long clubId, String memberId) {
        // 1. Service에서 순수 엔티티 조회
        Club club = clubManagementQueryService.retrieveClub(clubId);

        // 2. 운영진 권한 확인
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        boolean isStaff = clubMember.isStaff();
        if (!isStaff) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. DTO 변환 후 반환
        return ClubManagementConverter.toClubDetailDTO(club, isStaff);
    }

    public ClubResponseDTO.ClubMemberList retrieveClubMemberList(
            Long clubId,
            String memberId,
            String clubMemberStatus,
            Long cursorId
    ) {
        clubManagementQueryService.validateClub(clubId);
        ClubMember requester = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!requester.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        CursorResult<ClubMember> clubMemberCursorResult = CursorPagingHelper.getPage(
                size -> clubMemberQueryService.retrieveClubMembers(clubId, clubMemberStatus, cursorId, size),
                ClubMember::getId,
                DEFAULT_PAGE_SIZE
        );
        List<ClubMember> clubMembers = clubMemberCursorResult.content();
        List<String> memberIds = extractMemberIds(clubMembers);

        Map<String, MemberExternalDTO.BasicInfo> memberInfoMap = memberAPI.fetchMemberBasicInfoByMemberIds(memberIds);

        List<ClubResponseDTO.ClubMember> dtoList = clubMembers.stream()
                .map(cm -> {
                    MemberExternalDTO.BasicInfo memberInfo = memberInfoMap.get(cm.getMemberId());
                    return ClubManagementConverter.toClubMemberDTO(cm, memberInfo);
                })
                .toList();

        return ClubResponseDTO.ClubMemberList.builder()
                .clubMembers(dtoList)
                .hasNext(clubMemberCursorResult.hasNext())
                .nextCursor(clubMemberCursorResult.nextCursor())
                .pageSize(dtoList.size())
                .isStaff(true) // 항상 true
                .build();
    }

    private List<String> extractMemberIds(List<ClubMember> clubMembers) {
        if (clubMembers == null) {
            return List.of();
        }
        return clubMembers.stream()
                .map(checkmo.clubManagement.internal.entity.ClubMember::getMemberId)
                .distinct()
                .toList();
    }

    public Boolean isClubMemberStaff(Long clubId, String memberId) {
        clubManagementQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        return clubMember.isStaff();
    }

}
