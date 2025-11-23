package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.ClubManagementExternalDTO.BasicInfo;
import checkmo.clubManagement.ClubManagementExternalDTO.ClubList;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
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
public class ClubMemberQueryService {

    private final ClubMemberRepository clubMemberRepository;

    public ClubMember validateClubMember(Long clubId, String memberId) throws ClubManagementException {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .orElseThrow(() -> new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_ONLY));
    }

    public ClubList retrieveClubList(String memberId) {
        // 회원ID를 통해 JPQL로 클럽 ID와 이름을 조회하고 DTO로 변환
        var clubIdAndNameByMemberId = clubMemberRepository.findClubIdAndNameByMemberId(memberId);

        // Object[] -> BasicInfo 변환
        var myClubInfoList = clubIdAndNameByMemberId.stream()
                .map(row -> new BasicInfo((Long) row[0], (String) row[1]))
                .toList();

        return ClubList.builder()
                .clubList(myClubInfoList)
                .build();
    }

    public List<ClubMember> retrieveClubMembers(String memberId, Long cursorId, Integer size) {
        return clubMemberRepository.findClubMembersByMemberIdOrderByIdAsc(memberId, cursorId, Pageable.ofSize(size));
    }

    public Map<Long, ClubMember.ClubMemberStatus> retrieveClubMemberStatusByClubIds(
            String memberId, List<Long> clubIds) {
        // clubMemberRepository에서 clubId IN :clubIds AND memberId = :memberId 조건으로 여러 상태를 한 번에 조회
        List<ClubMember> members = clubMemberRepository.findAllByMemberIdAndClubIdIn(memberId, clubIds);

        // Map<clubId, ClubMemberStatus> 형태로 변환 후 반환
        return members.stream()
                .collect(Collectors.toMap(clubMember -> clubMember.getClub().getId(), ClubMember::getClubMemberStatus));
    }

    public List<ClubMember> retrieveClubMembers(Long clubId, String status, Long cursorId, Integer size) {
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

    public List<String> retrieveActiveMemberIds(Long clubId) {
        return clubMemberRepository.findActiveMemberIdsByClubId(clubId);
    }

    public List<ClubMember> retrieveClubMembers(Set<Long> clubMemberIds) {
        if (clubMemberIds == null) {
            return List.of();
        }
        return clubMemberRepository.findAllById(clubMemberIds);
    }
}
