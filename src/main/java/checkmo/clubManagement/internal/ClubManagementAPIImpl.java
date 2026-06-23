package checkmo.clubManagement.internal;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.service.command.ClubManagementCommandService;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubManagementAPIImpl implements ClubManagementAPI {

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubManagementCommandService clubManagementCommandService;
    private final ClubMemberQueryService clubMemberQueryService;

    @Override
    public void validateClub(Long clubId) throws ClubManagementException {
        clubManagementQueryService.validateClub(clubId);
    }

    @Override
    public String fetchClubName(Long clubId) throws ClubManagementException {
        return clubManagementQueryService.validateClub(clubId).getName();
    }

    @Override
    public Map<Long, String> fetchClubNamesByClubIds(List<Long> clubIds) {
        if (clubIds == null || clubIds.isEmpty()) {
            return Map.of();
        }
        return clubManagementQueryService.retrieveClubNamesByIds(clubIds);
    }

    @Override
    public List<String> fetchActiveMemberIds(Long clubId) {
        return clubMemberQueryService.retrieveActiveMemberIds(clubId);
    }

    @Override
    public boolean isStaffClubMember(Long clubId, String memberId) {
        return clubMemberQueryService.findClubMember(clubId, memberId)
                .map(ClubMember::isStaff)
                .orElse(false);
    }

    @Override
    public void validateStaffClubMember(Long clubId, String memberId) throws ClubManagementException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        if (!clubMember.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }
    }

    @Override
    public void validateActiveClubMembers(Long clubId, Set<Long> clubMemberIds) throws ClubManagementException {
        if (clubId == null) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_NOT_FOUND);
        }
        if (clubMemberIds == null || clubMemberIds.isEmpty()) {
            return;
        }
        List<ClubMember> clubMembers = clubMemberQueryService.retrieveClubMembers(clubMemberIds);
        if (clubMembers.size() != clubMemberIds.size()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_NOT_FOUND);
        }
        clubMembers.forEach(clubMember -> {
            if (!clubMember.getClub().getId().equals(clubId)) {
                throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_NOT_FOUND);
            }
            if (!clubMember.isActive()) {
                throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
            }
        });
    }

    @Override
    public Long fetchActiveClubMemberId(Long clubId, String memberId) {
        try {
            ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

            if (!clubMember.isActive()) {
                return null;
            }

            return clubMember.getId();
        } catch (ClubManagementException e) {
            return null; // 클럽 또는 멤버가 존재하지 않거나, ACTIVE 상태가 아닌 경우 null 반환
        }
    }

    @Override
    public Long validateAndFetchActiveClubMemberId(Long clubId, String memberId) throws ClubManagementException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        if (!clubMember.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        return clubMember.getId();
    }

    @Override
    public MembershipInfo fetchMembershipInfo(Long clubId, String memberId)
            throws ClubManagementException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        return ClubManagementConverter.toMembershipDTO(clubMember);
    }

    @Override
    public Map<Long, MembershipInfo> fetchMembershipInfoByClubMemberIds(
            Set<Long> clubMemberIds
    ) throws ClubManagementException {
        if (clubMemberIds == null) {
            return Map.of();
        }
        List<ClubMember> clubMembers = clubMemberQueryService.retrieveClubMembers(clubMemberIds);
        if (clubMembers.size() != clubMemberIds.size()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_NOT_FOUND);
        }
        return ClubManagementConverter.toMembershipDTOMap(clubMembers);
    }

    @Override
    public List<MembershipInfo> fetchAllActiveMembershipInfo(Long clubId) {
        List<ClubMember> clubMembers
                = clubMemberQueryService.retrieveClubMembers(clubId, ClubMemberStatus.activeStatuses());
        if (clubMembers == null || clubMembers.isEmpty()) {
            return List.of();
        }
        return ClubManagementConverter.toMembershipDTOList(clubMembers);
    }

    @Override
    @Transactional
    public void touchLastActivity(Long clubId, LocalDateTime lastActivityTime) {
        clubManagementCommandService.updateLastActivityTime(clubId, lastActivityTime);
    }

    @Override
    public ClubManagementExternalDTO.DisplayInfo fetchDisplayInfo(Long clubId) throws ClubManagementException {
        Club club = clubManagementQueryService.validateClub(clubId);

        return ClubManagementExternalDTO.DisplayInfo.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .clubImageUrl(club.getProfileImgUrl())
                .build();
    }

    @Override
    public Map<Long, ClubManagementExternalDTO.DisplayInfo> fetchDisplayInfoByClubIds(List<Long> clubIds) {
        if (clubIds == null || clubIds.isEmpty()) {
            return Map.of();
        }

        return clubManagementQueryService.retrieveClubs(clubIds).stream()
                .collect(Collectors.toMap(
                        Club::getId,
                        club -> ClubManagementExternalDTO.DisplayInfo.builder()
                                .clubId(club.getId())
                                .clubName(club.getName())
                                .clubImageUrl(club.getProfileImgUrl())
                                .build()
                ));
    }
}
