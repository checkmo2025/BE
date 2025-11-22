package checkmo.clubManagement.internal.service.query;

import static checkmo.clubManagement.ClubManagementExternalDTO.BasicInfo;
import static checkmo.clubManagement.ClubManagementExternalDTO.ClubList;

import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.member.MemberAPI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberQueryServiceImpl implements ClubMemberQueryService {

    // Domain level 2
    private final MemberAPI memberAPI;

    private final ClubMemberRepository clubMemberRepository;

    @Override
    public ClubMember validateClubMember(Long clubId, String memberId) throws ClubManagementException {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .orElseThrow(() -> new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_ONLY));
    }

    @Override
    public ClubList getMyClubList(String memberId) {
        // 회원ID를 통해 JPQL로 클럽 ID와 이름을 조회하고 DTO로 변환
        var clubIdAndNameByMemberId = clubMemberRepository.findClubIdAndNameByMemberId(memberId);

        // Object[] -> ClubNoticeExternalDTO.MyClubInfo 변환
        var myClubInfoList = clubIdAndNameByMemberId.stream()
                .map(row -> new BasicInfo((Long) row[0], (String) row[1]))
                .toList();

        return ClubList.builder()
                .clubList(myClubInfoList)
                .build();
    }

    @Override
    public List<ClubMember> getMyPageClubList(String memberId, Long cursorId, Integer size) {
        return clubMemberRepository.findClubMembersByMemberIdOrderByIdAsc(memberId, cursorId, Pageable.ofSize(size));
    }

    @Override
    public Map<Long, ClubMember.ClubMemberStatus> getMemberStatuses(String memberId, List<Long> clubIds) {
        // clubMemberRepository에서 clubId IN :clubIds AND memberId = :memberId 조건으로 여러 상태를 한 번에 조회
        List<ClubMember> members = clubMemberRepository.findAllByMemberIdAndClubIdIn(memberId, clubIds);

        // Map<clubId, ClubMemberStatus> 형태로 변환 후 반환
        return members.stream()
                .collect(Collectors.toMap(ClubMember::getClubId, ClubMember::getClubMemberStatus));
    }

    @Override
    public List<ClubMember> getClubMemberListByStatus(Long clubId, String status, Long cursorId, Integer size) {
        List<ClubMember.ClubMemberStatus> clubMemberStatus;
        if ("ALL".equalsIgnoreCase(status)) {
            clubMemberStatus = null;
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            clubMemberStatus = List.of(ClubMember.ClubMemberStatus.MEMBER, ClubMember.ClubMemberStatus.STAFF);
        } else {
            try {
                clubMemberStatus = List.of(ClubMember.ClubMemberStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS);
            }
        }

        return clubMemberRepository.findClubMembersByClubIdInClubMemberStatusOrderByIdDesc(
                clubId,
                clubMemberStatus,
                cursorId,
                size
        );
    }

    @Override
    public List<String> getActiveMemberIds(Long clubId) {
        return clubMemberRepository.findActiveMemberIdsByClubId(clubId);
    }

    @Override
    public List<ClubMember> getClubMembersByIds(Set<Long> clubMemberIds) {
        if (clubMemberIds == null) {
            return List.of();
        }
        return clubMemberRepository.findAllById(clubMemberIds);
    }
}
