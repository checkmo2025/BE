package checkmo.member.internal;

import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.BasicInfoWithFollow;
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

    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;

    private final MemberQueryFacade memberQueryFacade;

    @Override
    public String fetchMemberId(String nickname) {
        return memberQueryService.getMemberIdByNickname(nickname);
    }

    @Override
    public String fetchNickname(String memberId) {
        return memberQueryService.getMemberNicknameById(memberId);
    }

    @Override
    public Map<String, String> fetchNicknameByMemberIds(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        return memberQueryService.getMemberNicknamesByMemberIds(memberIds);
    }

    @Override
    public MemberExternalDTO.BasicInfo fetchMemberBasicInfo(String memberId) {
        Member member = memberQueryService.getMemberBasicInfo(memberId);
        MemberResponseDTO.MemberProfileWithProfileImage profileDTO = MemberConverter.toMemberProfileWithProfileImage(
                member);

        return MemberExternalDTO.BasicInfo.builder()
                .nickname(profileDTO.getNickname())
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .build();
    }

    @Override
    public Map<String, MemberExternalDTO.BasicInfo> fetchMemberBasicInfoByMemberIds(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        // 1. Repository를 통해 IN 쿼리로 모든 회원 정보 조회 (Projection 사용)
        List<MemberBasicInfoProjection> results = memberQueryService.getMemberBasicInfoMapForShare(memberIds);

        // 2. 조회된 Projection 리스트를 Map으로 변환
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
    public BasicInfoWithFollow fetchMemberBasicInfoWithFollow(
            String targetMemberId,
            String currentMemberId
    ) {
        // 팔로우 상태를 조회
        boolean isFollowing = memberFollowQueryService.isFollowing(currentMemberId, targetMemberId);

        var basicInfoDTO = fetchMemberBasicInfo(targetMemberId);

        return BasicInfoWithFollow.builder()
                .nickname(basicInfoDTO.getNickname())
                .profileImageUrl(basicInfoDTO.getProfileImageUrl())
                .following(isFollowing)
                .build();
    }

    @Override
    public Map<String, BasicInfoWithFollow> fetchMemberBasicInfoWithFollowByMemberId(
            List<String> targetMemberIds,
            String currentMemberId
    ) {
        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Map.of();
        }

        // 1. Facade에서 내부 DTO로 배치 조회
        List<MemberResponseDTO.MemberProfileWithFollow> profiles
                = memberQueryFacade.getMemberProfiles(targetMemberIds, currentMemberId);

        // 2. 내부 DTO → 외부 DTO 변환 후 Map으로 변환
        // targetMemberIds와 profiles는 순서가 일치하므로 zip 형태로 매핑
        Map<String, BasicInfoWithFollow> result = new java.util.HashMap<>();
        for (int i = 0; i < targetMemberIds.size() && i < profiles.size(); i++) {
            String memberId = targetMemberIds.get(i);
            MemberResponseDTO.MemberProfileWithFollow profile = profiles.get(i);
            result.put(memberId, MemberConverter.toMemberProfileWithFollowStatus(profile));
        }

        return result;
    }

    @Override
    public List<String> fetchFollowingMemberIds(String memberId) {
        return memberFollowQueryService.getFollowingMemberIds(memberId);
    }

}
