package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.clubManagement.internal.repository.projection.ClubIdAndName;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberQueryService {

    private final ClubMemberRepository clubMemberRepository;

    public ClubMember validateClubMember(Long clubId, Long memberId) throws ClubManagementException {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .orElseThrow(() -> new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_NOT_FOUND));
    }

    public ClubMember validateClubMemberById(Long clubId, Long clubMemberId) throws ClubManagementException {
        return clubMemberRepository.findByIdAndClubId(clubMemberId, clubId)
                .orElseThrow(() -> new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_NOT_FOUND));
    }

    public List<ClubIdAndName> retrieveAllActiveClubsByMemberId(Long memberId) {
        EnumSet<ClubMemberStatus> activeStatuses = ClubMemberStatus.activeStatuses();
        return clubMemberRepository.findClubIdAndNameByMemberIdAndStatuses(memberId, activeStatuses);
    }

    public Optional<ClubMember> findClubMember(Long clubId, Long memberId) {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId);
    }

    public Map<Long, ClubMemberStatus> retrieveClubMemberStatusByClubIds(
            Long memberId,
            List<Long> clubIds
    ) {
        // clubMemberRepository에서 clubId IN :clubIds AND memberId = :memberId 조건으로 여러 상태를 한 번에 조회
        List<ClubMember> members = clubMemberRepository.findAllByMemberIdAndClubIdIn(memberId, clubIds);

        // Map<clubId, ActiveClubMemberStatus> 형태로 변환 후 반환
        // 만약 clubMember가 없으면 해당 clubId는 키에 포함되지 않음
        return members.stream()
                .collect(Collectors.toMap(
                        clubMember -> clubMember.getClub().getId(),
                        ClubMember::getClubMemberStatus,
                        (a, b) -> a
                ));
    }

    public List<ClubMember> retrieveClubMembers(Long clubId, EnumSet<ClubMemberStatus> statuses, Long cursorId, int size) {
        return clubMemberRepository.findByClubIdAndStatuses(clubId, statuses, cursorId, PageRequest.of(0, size));
    }

    public Page<ClubMember> retrieveClubMembers(Long clubId, EnumSet<ClubMemberStatus> statuses, Pageable pageable) {
        return clubMemberRepository.findClubIdAndStatusesInOrderByDesc(
                clubId,
                statuses,
                pageable
        );
    }

    public List<ClubMember> retrieveClubMembers(Long clubId, EnumSet<ClubMemberStatus> statuses) {
        return clubMemberRepository.findByClubIdAndStatuses(clubId, statuses, null, Pageable.unpaged());
    }

    public List<ClubMember> retrieveClubMembersByClubIds(List<Long> clubIds, EnumSet<ClubMemberStatus> statuses) {
        if (clubIds == null || clubIds.isEmpty()) {
            return List.of();
        }
        return clubMemberRepository.findByClubIdInAndStatuses(clubIds, statuses);
    }

    public long countActiveClubMembers(Long clubId) {
        return clubMemberRepository.countByClubIdAndClubMemberStatusIn(clubId, ClubMemberStatus.activeStatuses());
    }

    public List<Long> retrieveActiveMemberIds(Long clubId) {
        return clubMemberRepository.findByClubIdAndStatuses(clubId, ClubMemberStatus.activeStatuses(), null,
                        Pageable.unpaged())
                .stream()
                .map(ClubMember::getMemberId)
                .toList();
    }

    public List<ClubMember> retrieveClubMembers(Set<Long> clubMemberIds) {
        if (clubMemberIds == null) {
            return List.of();
        }
        return clubMemberRepository.findAllById(clubMemberIds);
    }
}
