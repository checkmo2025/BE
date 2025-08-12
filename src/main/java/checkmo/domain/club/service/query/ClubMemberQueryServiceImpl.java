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

import java.util.*;
import java.util.stream.Collectors;

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
