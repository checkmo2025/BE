package checkmo.clubManagement.internal;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO.Membership;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubManagementAPIImpl implements ClubManagementAPI {

    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    @Override
    public void validateClub(Long clubId) throws ClubManagementException {
        clubQueryService.validateClub(clubId);
    }

    @Override
    public ClubManagementExternalDTO.MyClubList getMyClubListForShare(String memberId) {
        return clubMemberQueryService.getMyClubList(memberId);
    }

    @Override
    public List<String> getActiveMemberIds(Long clubId) {
        return clubMemberQueryService.getActiveMemberIds(clubId);
    }

    @Override
    public void validateStaffClubMember(Long clubId, String memberId) throws ClubManagementException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        if (!clubMember.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }
    }

    @Override
    public Long getActiveClubMemberInfo(Long clubId, String memberId) throws ClubManagementException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        if (!clubMember.isActive()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        return clubMember.getId();
    }

    @Override
    public Membership getClubMembershipInfo(Long clubId, String memberId) throws ClubManagementException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        return ClubManagementConverter.toMembershipDTO(clubMember);
    }

    @Override
    public Map<Long, Membership> getClubMembershipInfos(Set<Long> clubMemberIds) throws ClubManagementException {
        if (clubMemberIds == null) {
            return Map.of();
        }
        List<ClubMember> clubMembers = clubMemberQueryService.getClubMembersByIds(clubMemberIds);
        if (clubMembers.size() != clubMemberIds.size()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_NOT_FOUND);
        }
        return ClubManagementConverter.toMembereshipDTOMap(clubMembers);
    }

    @Override
    public List<Membership> getClubMembersByStatus(Long clubId, Long cursorId, Integer size) {
        List<ClubMember> clubMembers
                = clubMemberQueryService.getClubMemberListByStatus(clubId, "ACTIVE", cursorId, size);
        return ClubManagementConverter.toMembershipDTOList(clubMembers);
    }
}
