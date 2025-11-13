package checkmo.member.internal;

import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Member;
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

    @Override
    public MemberExternalDTO.BasicInfo getMemberBasicInfoForShare(String memberId) {
        Member member = memberQueryService.getMemberBasicInfo(memberId);
        MemberResponseDTO.MemberProfileWithProfileImage profileDTO = MemberConverter.toMemberProfileWithProfileImage(member);

        return MemberConverter.toBasicInfo(profileDTO);
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

    @Override
    public MemberExternalDTO.WithFollowStatus getMemberWithFollowStatusForShare(String targetMemberId,
                                                                                String currentMemberId) {
        // 팔로우 상태를 조회
        boolean isFollowing = memberFollowQueryService.isFollowing(currentMemberId, targetMemberId);

        var basicInfoDTO = getMemberBasicInfoForShare(targetMemberId);
        return MemberConverter.toWithFollowStatus(basicInfoDTO, isFollowing);
    }

    @Override
    public Map<String, MemberExternalDTO.WithFollowStatus> getMemberWithFollowStatusMapForShare(
            List<String> targetMemberIds, String currentMemberId) {
        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Map.of();
        }

        // 1. Facade에서 내부 DTO로 배치 조회
        List<MemberResponseDTO.MemberProfileWithFollow> profiles = memberQueryFacade.getMemberProfiles(targetMemberIds,
                currentMemberId);

        // 2. 내부 DTO → 외부 DTO 변환 후 Map으로 변환
        // targetMemberIds와 profiles는 순서가 일치하므로 zip 형태로 매핑
        Map<String, MemberExternalDTO.WithFollowStatus> result = new java.util.HashMap<>();
        for (int i = 0; i < targetMemberIds.size() && i < profiles.size(); i++) {
            String memberId = targetMemberIds.get(i);
            MemberResponseDTO.MemberProfileWithFollow profile = profiles.get(i);
            result.put(memberId, MemberConverter.toMemberExternalDTOWithFollowStatus(profile));
        }

        return result;
    }

}
