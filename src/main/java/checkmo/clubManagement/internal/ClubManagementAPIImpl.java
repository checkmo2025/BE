package checkmo.clubManagement.internal;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipDTO;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
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
    public Long getClubInfo(Long clubId) throws GeneralException {
        Club club = clubQueryService.validateClub(clubId);
        return club.getId();
    }

    @Override
    public ClubManagementExternalDTO.MyClubList getMyClubListForShare(String memberId) {
        return clubMemberQueryService.getMyClubList(memberId);
    }

    @Override
    public List<String> getClubMemberIds(Long clubId) {
        return clubMemberQueryService.getClubMemberIds(clubId);
    }

    @Override
    public boolean isMemberInClub(String memberId, Long clubId) {
        return clubMemberQueryService.isMemberInClub(memberId, clubId);
    }

    @Override
    public Long getStaffClubMemberInfo(Long clubId, String memberId) throws GeneralException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        return clubMember.getId();
    }

    @Override
    public Long getActiveClubMemberInfo(Long clubId, String memberId) throws GeneralException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        return clubMember.getId();
    }

    @Override
    public MembershipDTO getClubMembershipInfo(Long clubId, String memberId) throws GeneralException {
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        return ClubManagementConverter.fromClubMembertoMembershipDTO(clubMember);
    }

    @Override
    public Map<Long, MembershipDTO> getClubMembershipInfos(Set<Long> clubMemberIds) throws GeneralException {
        if (clubMemberIds == null) {
            return Map.of();
        }
        List<ClubMember> clubMembers = clubMemberQueryService.getClubMembersByIds(clubMemberIds);
        if (clubMembers.size() != clubMemberIds.size()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_NOT_FOUND);
        }
        return ClubManagementConverter.fromClubMembertoMembereshipDTO(clubMembers);
    }

    @Override
    public List<MembershipDTO> getClubMembersByStatus(Long clubId, Long cursorId, int size) {
        List<ClubMember> clubMembers
                = clubMemberQueryService.getClubMemberListByStatus(clubId, "ACTIVE", cursorId, size);
        return ClubManagementConverter.fromClubMemberToMembershipDTO(clubMembers);
    }
}
