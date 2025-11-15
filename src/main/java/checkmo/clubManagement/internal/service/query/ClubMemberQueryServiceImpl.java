package checkmo.clubManagement.internal.service.query;

import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubManagement.internal.converter.ClubManagementConverter;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.repository.ClubMemberRepository;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberAPI;
import java.util.ArrayList;
import java.util.HashMap;
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

    // 자신의 Repository
    private final ClubMemberRepository clubMemberRepository;

    @Override
    public ClubMember validateClubMember(Long clubId, String memberId) throws GeneralException {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_MEMBER_ONLY));
    }

    @Override
    public ClubManagementExternalDTO.MyClubList getMyClubList(String memberId) {

        // 회원ID를 통해 JPQL로 클럽 ID와 이름을 조회하고 DTO로 변환
        var clubIdAndNameByMemberId = clubMemberRepository.findClubIdAndNameByMemberId(memberId);

        // Object[] -> ClubNoticeExternalDTO.MyClubInfo 변환
        var myClubInfoList = clubIdAndNameByMemberId.stream()
                .map(row -> new ClubManagementExternalDTO.MyClubInfo((Long) row[0], (String) row[1]))
                .toList();

        return ClubManagementConverter.fromClubInfoListToMyClubList(myClubInfoList);
    }

    @Override
    public List<Long> getMyClubListIds(String memberId) {
        return clubMemberRepository.findClubIdsByMemberId(memberId);
    }

    @Override
    public List<ClubMember> getMyPageClubList(String memberId, Long cursorId, Integer size) {
        return clubMemberRepository.findClubMembersByMemberIdOrderByIdAsc(memberId, cursorId, Pageable.ofSize(size));
    }

    @Override
    public ClubMember.ClubMemberStatus getMemberStatusInClub(String memberId, Long clubId) {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .map(ClubMember::getClubMemberStatus)
                .orElse(null); // 존재하지 않으면 null 반환
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
            clubMemberStatus = null; // ALL
        } else if ("ACTIVE".equalsIgnoreCase(status)) { // ACTIVE는 내부적으로 사용할 예정
            clubMemberStatus = List.of(ClubMember.ClubMemberStatus.MEMBER, ClubMember.ClubMemberStatus.STAFF);
        } else {
            try {
                clubMemberStatus = List.of(ClubMember.ClubMemberStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new GeneralException(ErrorStatus.CLUB_MEMBER_INVALID_STATUS);
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
    public Map<String, ClubMember> getNicknameToClubMember(Long clubId, List<String> nicknames)
            throws GeneralException {
        if (nicknames == null || nicknames.isEmpty()) {
            return Map.of();
        }

        Map<String, String> nicknameToMemberId = memberAPI.getMemberIdsByNicknames(nicknames);
        Map<String, ClubMember> memberIdToClubMember = getMemberIdToClubMember(clubId,
                nicknameToMemberId.values().stream().toList());
        Map<String, ClubMember> nicknameToClubMember = new HashMap<>(nicknameToMemberId.size());
        List<String> missing = new ArrayList<>();
        for (String nickname : nicknames) {
            String memberId = nicknameToMemberId.get(nickname);
            ClubMember clubMember = (memberId == null) ? null : memberIdToClubMember.get(memberId);
            if (clubMember == null) {
                missing.add(nickname);
            } else {
                nicknameToClubMember.put(nickname, clubMember);
            }
        }
        if (!missing.isEmpty()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_NOT_FOUND, missing.toString()); // TODO: 잘못에러 메시지 확인
        }
        return nicknameToClubMember;
    }

    private Map<String, ClubMember> getMemberIdToClubMember(Long clubId, List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }
        List<ClubMember> results = clubMemberRepository.findClubMembersByClubIdAndMemberIdIn(clubId, memberIds);
        return results.stream()
                .collect(Collectors.toMap(
                        ClubMember::getMemberId,
                        cm -> cm)
                );
    }

    @Override
    public boolean isMemberInClub(String memberId, Long clubId) {
        return clubMemberRepository.isMemberInClub(memberId, clubId);
    }

    @Override
    public List<String> getClubMemberIds(Long clubId) {
        return clubMemberRepository.getClubMemberIds(clubId);
    }

    @Override
    public List<ClubMember> getClubMembersByIds(Set<Long> clubMemberIds) {
        if (clubMemberIds == null) {
            return List.of();
        }
        return clubMemberRepository.findAllById(clubMemberIds);
    }
}
