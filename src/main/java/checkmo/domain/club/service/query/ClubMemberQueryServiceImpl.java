package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.repository.ClubMemberRepository;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.ClubSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMemberQueryServiceImpl implements ClubMemberQueryService {

    private final ClubMemberRepository clubMemberRepository;
    private final MemberQueryFacade memberQueryFacade;

    @Override
    public ClubMember validateClubMember(Long clubId, String memberId) throws GeneralException {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLUB_MEMBER_ONLY));
    }

    @Override
    public ClubSharedDTO.MyClubList getMyClubList(String memberId) {

        // 회원ID를 통해 JPQL로 클럽 ID와 이름을 조회하고 DTO로 변환
        var clubIdAndNameByMemberId = clubMemberRepository.findClubIdAndNameByMemberId(memberId);

        // Object[] -> ClubSharedDTO.MyClubInfo 변환
        var myClubInfoList = clubIdAndNameByMemberId.stream()
                .map(row -> new ClubSharedDTO.MyClubInfo((Long) row[0], (String) row[1]))
                .toList();

        return ClubConverter.fromClubInfoListToMyClubList(myClubInfoList);
    }

    /**
     * 특정 회원이 해당 클럽에서 어떤 상태(등급)인지 조회합니다.
     *
     * @param memberId 회원 ID
     * @param clubId   클럽 ID
     * @return ClubMemberStatus (MEMBER, STAFF 등) 또는 null (회원 아님)
     */
    @Override
    public ClubMember.ClubMemberStatus getMemberStatusInClub(String memberId, Long clubId) {
        return clubMemberRepository.findByClubIdAndMemberId(clubId, memberId)
                .map(ClubMember::getClubMemberStatus)
                .orElse(null); // 존재하지 않으면 null 반환
    }

    /**
     * 특정 회원이 여러 클럽에서의 상태를 한꺼번에 조회합니다.
     *
     * @param memberId 회원 ID
     * @param clubIds 클럽 ID 리스트
     * @return 클럽 ID별 회원 상태 맵
     */
    @Override
    public Map<Long, ClubMember.ClubMemberStatus> getMemberStatuses(String memberId, List<Long> clubIds) {

        // clubMemberRepository에서 clubId IN :clubIds AND memberId = :memberId 조건으로 여러 상태를 한 번에 조회
        List<ClubMember> members = clubMemberRepository.findAllByMemberIdAndClubIdIn(memberId, clubIds);

        // Map<clubId, ClubMemberStatus> 형태로 변환 후 반환
        return members.stream()
                .collect(Collectors.toMap(ClubMember::getClubId, ClubMember::getClubMemberStatus));
    }


    @Override
    public Map<String, ClubMember> getNicknameToClubMember(Long clubId, Collection<String> nicknames) throws GeneralException {
        Map<String, String> nicknameToMemberId = memberQueryFacade.getMemberIdsByNicknames(nicknames);
        Map<String, ClubMember> memberIdToClubMember = getMemberIdToClubMember(clubId, nicknameToMemberId.values());
        Map<String, ClubMember> nicknameToClubMember = new HashMap<>();
        List<String> missing = new ArrayList<>();
        for (String nickname : nicknames) {
            String memberId = nicknameToMemberId.get(nickname);
            ClubMember clubMember = (memberId == null) ? null : memberIdToClubMember.get(memberId);
            if (clubMember == null) missing.add(nickname);
            else nicknameToClubMember.put(nickname, clubMember);
        }
        if (!missing.isEmpty()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_NOT_FOUND, missing.toString()); // TODO: 잘못에러 메시지 확인
        }
        return nicknameToClubMember;
    }

    private Map<String, ClubMember> getMemberIdToClubMember(Long clubId, Collection<String> memberIds) {
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
}
