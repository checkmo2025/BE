package checkmo.member.internal;

import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.service.MemberQueryFacade;
import checkmo.member.internal.service.query.MemberFollowQueryService;
import checkmo.member.internal.service.query.MemberQueryService;
import checkmo.member.web.dto.MemberResponseDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAPIImpl implements MemberAPI {

    // 자신의 QueryService
    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;

    // 내부 Facade (배치 조회 로직 재사용)
    private final MemberQueryFacade memberQueryFacade;

    // 자신의 Repository (프록시용, TODO: 해결 불가한가?)
    private final MemberRepository memberRepository;

    @Override
    public String getMemberIdByNickname(String nickname) {
        return memberQueryService.getMemberIdByNickname(nickname);
    }

    @Override
    public Map<String, String> getMemberIdsByNicknames(List<String> nicknames) {
        if (nicknames == null || nicknames.isEmpty()) {
            return Map.of();
        }
        return memberQueryService.getMemberIdsByNicknames(nicknames);
    }

    /**
     * 공유용 기본 회원 정보 조회 (외부용)
     *
     * @param memberId 조회할 회원 ID
     * @return MemberExternalDTO.BasicInfo
     */
    @Override
    public MemberExternalDTO.BasicInfo getMemberBasicInfoForShare(String memberId) {
        Member member = memberQueryService.getMemberBasicInfo(memberId);
        MemberResponseDTO.MemberProfileResponseDTO profileDTO = MemberConverter.toMemberProfileResponseDTO(member);

        return MemberConverter.toBasicInfoDTO(profileDTO);
    }

    @Override
    public Map<String, MemberExternalDTO.BasicInfo> getMemberBasicInfoMapForShare(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        // 1. Repository를 통해 IN 쿼리로 모든 회원 정보 조회 (Projection 사용)
        List<MemberBasicInfoProjection> results = memberQueryService.getMemberBasicInfoMapForShare(memberIds);

        // 2. 조회된 Projection 리스트를 Map으로 변환
        // memberId를 key로, BasicInfoDTO를 value로 사용
        return results.stream()
                .collect(Collectors.toMap(
                        MemberBasicInfoProjection::getId,
                        projection -> MemberExternalDTO.BasicInfo.builder()
                                .nickname(projection.getNickName())
                                .profileImageUrl(projection.getImgUrl())
                                .build()
                ));
    }

    /**
     * 공유용 기본 회원 정보 + 팔로우 상태 조회 (외부용)
     *
     * @param targetMemberId  조회 대상 회원 ID
     * @param currentMemberId 현재 로그인한 회원 ID
     * @return MemberExternalDTO.WithFollowStatusDTO
     */
    @Override
    public MemberExternalDTO.WithFollowStatus getMemberWithFollowStatusForShare(String targetMemberId,
                                                                                String currentMemberId) {
        // 팔로우 상태를 조회
        boolean isFollowing = memberFollowQueryService.isFollowing(currentMemberId, targetMemberId);

        var basicInfoDTO = getMemberBasicInfoForShare(targetMemberId);
        return MemberConverter.toWithFollowStatusDTO(basicInfoDTO, isFollowing);
    }

    @Override
    public Map<String, MemberExternalDTO.WithFollowStatus> getMemberWithFollowStatusMapForShare(
            List<String> targetMemberIds, String currentMemberId) {
        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Map.of();
        }

        // 1. Facade에서 내부 DTO로 배치 조회
        List<MemberResponseDTO.MemberProfile> profiles = memberQueryFacade.getMemberProfiles(targetMemberIds,
                currentMemberId);

        // 2. 내부 DTO → 외부 DTO 변환 후 Map으로 변환
        // targetMemberIds와 profiles는 순서가 일치하므로 zip 형태로 매핑
        Map<String, MemberExternalDTO.WithFollowStatus> result = new java.util.HashMap<>();
        for (int i = 0; i < targetMemberIds.size() && i < profiles.size(); i++) {
            String memberId = targetMemberIds.get(i);
            MemberResponseDTO.MemberProfile profile = profiles.get(i);
            result.put(memberId, MemberConverter.toExternalDTO(profile));
        }

        return result;
    }

    @Override
    public Member findMemberReferenceById(String memberId) {
        return memberRepository.getReferenceById(memberId);
    }

    @Override
    public String getMemberNicknameById(String memberId) {
        return memberQueryService.getMemberNicknameById(memberId);
    }

    @Override
    public Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        return memberQueryService.getMemberNicknamesByMemberIds(memberIds);
    }

}
