package checkmo.clubManagement.internal;

import static checkmo.clubManagement.ClubManagementExternalDTO.ClubList;
import static checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.service.query.ClubManagementQueryService;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
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

    private final ClubManagementQueryService clubManagementQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    @Override
    public void validateClub(Long clubId) throws ClubManagementException {
        clubManagementQueryService.validateClub(clubId);
    }

    @Override
    public ClubList fetchMyClubs(String memberId) {
        return clubMemberQueryService.retrieveClubList(memberId);
    }

    @Override
    public List<String> fetchActiveMemberIds(Long clubId) {
        return clubMemberQueryService.retrieveActiveMemberIds(clubId);
    }

    @Override
    public void validateStaffClubMember(Long clubId, String memberId) throws ClubManagementException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        if (!clubMember.isStaff()) {
            throw new ClubManagementException(ClubManagementErrorStatus.CLUB_STAFF_ONLY);
        }
    }

    @Override
    public Long fetchActiveClubMemberId(Long clubId, String memberId) throws ClubManagementException {
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
        return ClubManagementConverter.toMembereshipDTOMap(clubMembers);
    }

    @Override
    public List<MembershipInfo> fetchActiveMembershipInfo(
            Long clubId, Long cursorId,
            Integer size
    ) {
        List<ClubMember> clubMembers
                = clubMemberQueryService.retrieveClubMembers(clubId, "ACTIVE", cursorId, size);
        return ClubManagementConverter.toMembershipDTOList(clubMembers);
    }
}
