package checkmo.clubManagement.internal.service;

import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.projection.ClubIdAndName;
import checkmo.clubManagement.internal.repository.projection.ClubRecommendation;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubRequestDTO.ClubMemberStatusFilter;
import checkmo.clubManagement.web.dto.ClubResponseDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO.*;
import checkmo.clubManagement.web.dto.myClub.MyClubResponseDTO;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.common.template.ExtractHelper;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ClubManagementQueryFacade {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final MemberAPI memberAPI;

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    public ClubResponseDTO.ClubDetail retrieveClubDetail(Long clubId, String memberId) {
        Club club = clubManagementQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        boolean isStaff = clubMember.isStaff();
        if (!isStaff) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        return ClubManagementConverter.toClubDetailDTO(club, true);
    }

    public ClubResponseDTO.ClubDetail retrieveClubHome(Long clubId) {
        Club club = clubManagementQueryService.validateClub(clubId);
        return ClubManagementConverter.toClubDetailDTO(club, true);
    }

    public ClubResponseDTO.ClubList retrieveClubList(
            String memberId,
            ClubRequestDTO.ClubSearchFilter filter,
            Long cursorId
    ) {
        CursorResult<Club> clubCursorResult = CursorPagingHelper.getPage(
                size -> clubManagementQueryService.retrieveClubs(filter, cursorId, size),
                Club::getId,
                DEFAULT_PAGE_SIZE
        );

        List<Club> clubs = clubCursorResult.content();
        List<Long> clubIds = ExtractHelper.extractDistinctList(clubs, Club::getId);

        // 클럽별 멤버 상태 배치 조회
        Map<Long, ClubMemberStatus> statusMap
                = (memberId == null || memberId.isBlank()) ?
                Map.of() : clubMemberQueryService.retrieveClubMemberStatusByClubIds(memberId, clubIds);

        List<ClubDetailWithMyStatus> clubList = clubs.stream()
                .map(club -> toClubDetailWithMyStatusDTO(club, statusMap))
                .toList();

        return ClubResponseDTO.ClubList.builder()
                .clubList(clubList)
                .hasNext(clubCursorResult.hasNext())
                .nextCursor(clubCursorResult.nextCursor())
                .build();
    }

    private ClubDetailWithMyStatus toClubDetailWithMyStatusDTO(
            Club club,
            Map<Long, ClubMemberStatus> statusMap
    ) {
        ClubMemberStatus rawStatus = statusMap.get(club.getId());
        ClubResponseDTO.MyClubMemberStatus myStatus =
                (rawStatus == null) ? MyClubMemberStatus.NONE : MyClubMemberStatus.valueOf(rawStatus.name());
        ClubResponseDTO.ClubDetail clubDetail = ClubManagementConverter.toClubDetailDTO(club, false);

        return ClubDetailWithMyStatus.builder()
                .club(clubDetail)
                .myStatus(myStatus)
                .build();
    }

    public MyClubResponseDTO.MyClubList retrieveMyClubList(String memberId) {
        List<ClubIdAndName> clubIdAndNames = clubMemberQueryService.retrieveAllActiveClubsByMemberId(memberId);
        List<MyClubResponseDTO.ClubInfo> clubInfoList = clubIdAndNames.stream()
                .map(c -> MyClubResponseDTO.ClubInfo.builder()
                        .clubId(c.getId())
                        .clubName(c.getName())
                        .build()
                )
                .toList();

        return MyClubResponseDTO.MyClubList.builder()
                .clubList(clubInfoList)
                .build();
    }

    public ClubResponseDTO.MyMembership retrieveMyMembership(Long clubId, String memberId) {
        clubManagementQueryService.validateClub(clubId);
        Optional<ClubMember> clubMemberOpt = clubMemberQueryService.findClubMember(clubId, memberId);
        if (clubMemberOpt.isEmpty()) {
            return toNoneMembershipResponse(clubId);
        }
        ClubMember clubMember = clubMemberOpt.get();

        MyClubMemberStatus myStatus = MyClubMemberStatus.valueOf(clubMember.getClubMemberStatus().name());
        return ClubResponseDTO.MyMembership.builder()
                .clubId(clubId)
                .myStatus(myStatus)
                .active(clubMember.isActive())
                .staff(clubMember.isStaff())
                .build();
    }

    private MyMembership toNoneMembershipResponse(Long clubId) {
        return MyMembership.builder()
                .clubId(clubId)
                .myStatus(MyClubMemberStatus.NONE)
                .active(false)
                .staff(false)
                .build();
    }

    public ClubResponseDTO.ClubMemberList retrieveClubMemberList(
            Long clubId,
            String memberId,
            ClubMemberStatusFilter statusFilter,
            Long cursorId
    ) {
        clubManagementQueryService.validateClub(clubId);
        ClubMember requester = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!requester.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }

        CursorResult<ClubMember> clubMemberCursorResult = CursorPagingHelper.getPage(
                size -> clubMemberQueryService.retrieveClubMembers(clubId, statusFilter.toClubMemberStatusesOrNull(),
                        cursorId, size),
                ClubMember::getId,
                DEFAULT_PAGE_SIZE
        );
        List<ClubMember> clubMembers = clubMemberCursorResult.content();
        List<String> memberIds = ExtractHelper.extractDistinctList(clubMembers, ClubMember::getMemberId);

        Map<String, MemberExternalDTO.DetailInfo> memberInfoMap = memberAPI.fetchMemberDetailInfoByMemberIds(memberIds);

        List<ClubResponseDTO.ClubMember> dtoList = clubMembers.stream()
                .map(cm -> {
                    MemberExternalDTO.DetailInfo memberInfo = memberInfoMap.get(cm.getMemberId());
                    return ClubManagementConverter.toClubMemberDTO(cm, memberInfo);
                })
                .toList();

        return ClubResponseDTO.ClubMemberList.builder()
                .clubMembers(dtoList)
                .hasNext(clubMemberCursorResult.hasNext())
                .nextCursor(clubMemberCursorResult.nextCursor())
                .build();
    }

    public ClubRecommendationList recommend(String memberId) {
        List<String> memberInterestCategories = memberAPI.fetchInterestCategory(memberId).getCategories();
        EnumSet<ClubInterestCategory> interestCategories = mapToClubInterestCategories(memberInterestCategories);

        LocalDateTime lastActivityAt = LocalDateTime.now().minusYears(1);

        List<ClubRecommendation> result
                = clubManagementQueryService.recommend(interestCategories, lastActivityAt, memberId);

        // 추천 결과 기반으로 클럽 정보 배치 조회
        List<Long> clubIds = ExtractHelper.extractDistinctList(result, ClubRecommendation::getClubId);
        List<Club> clubs = clubManagementQueryService.retrieveClubs(clubIds);
        Map<Long, Club> clubMap = clubs.stream().collect(Collectors.toMap(Club::getId, c -> c));

        List<ClubResponseDTO.ClubRecommendation> recommendations = IntStream.range(0, result.size())
                .mapToObj(i -> {
                    ClubRecommendation rec = result.get(i);
                    Club club = clubMap.get(rec.getClubId());
                    if (club == null) {
                        // 추천 결과에 클럽 정보가 없는 경우는 무시 (정상적으로는 발생하지 않아야 함)
                        return null;
                    }
                    ClubDetailWithMyStatus clubDTO = ClubResponseDTO.ClubDetailWithMyStatus.builder()
                            .club(ClubManagementConverter.toClubDetailDTO(club, false))
                            .myStatus(MyClubMemberStatus.NONE)
                            .build();
                    return ClubResponseDTO.ClubRecommendation.builder()
                            .rank(i + 1)
                            .clubInfo(clubDTO)
                            .overlapCount(rec.getOverlapCount())
                            .activeMemberCount(rec.getActiveMemberCount())
                            .lastActivityAt(rec.getLastActivityAt())
                            .build();
                })
                .toList();

        return ClubResponseDTO.ClubRecommendationList.builder()
                .recommendations(recommendations.stream().filter(Objects::nonNull).toList())
                .build();
    }

    private EnumSet<ClubInterestCategory> mapToClubInterestCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return EnumSet.noneOf(ClubInterestCategory.class);
        }
        EnumSet<ClubInterestCategory> result = EnumSet.noneOf(ClubInterestCategory.class);
        for (String category : categories) {
            if (category == null || category.isBlank()) {
                continue;
            }
            try {
                result.add(ClubInterestCategory.valueOf(category.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // 알 수 없는 카테고리는 무시
            }
        }
        return result;
    }

    public ClubPreviewList retrieveClubListByMemberNickname(String memberNickname) {
        String memberId = memberAPI.fetchMemberId(memberNickname);
        List<ClubIdAndName> clubIdAndNames = clubMemberQueryService.retrieveAllActiveClubsByMemberId(memberId);
        List<ClubInfo> clubInfoList = clubIdAndNames.stream()
                .map(c -> ClubInfo.builder()
                        .clubId(c.getId())
                        .clubName(c.getName())
                        .build())
                .toList();
        return ClubPreviewList.builder()
                .clubList(clubInfoList)
                .build();
    }
}
