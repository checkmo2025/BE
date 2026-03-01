package checkmo.member.internal;

import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.DetailInfo;
import checkmo.member.MemberExternalDTO.InterestCategoryInfo;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberInterestCategory;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.service.query.MemberFollowQueryService;
import checkmo.member.internal.service.query.MemberQueryService;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAPIImpl implements MemberAPI {

    private static final String WITHDRAWN_MEMBER_NICKNAME = "탈퇴한 회원";

    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;

    @Override
    public String fetchMemberId(String nickname) {
        return memberQueryService.retrieveMemberId(nickname);
    }

    @Override
    public String fetchNickname(String memberId) {
        if (memberId == null) {
            return WITHDRAWN_MEMBER_NICKNAME;
        }

        return memberQueryService.retrieveActiveMemberNickname(memberId)
                .orElse(WITHDRAWN_MEMBER_NICKNAME);
    }

    @Override
    public Map<String, String> fetchNicknameByMemberIds(List<String> memberIds) {
        List<String> distinctMemberIds = distinctNonNullIds(memberIds);
        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }

        Map<String, String> result = initializeWithdrawnNicknameMap(distinctMemberIds);

        result.putAll(memberQueryService.retrieveActiveMemberNicknameByMemberIds(distinctMemberIds));
        return result;
    }

    @Override
    public MemberExternalDTO.BasicInfo fetchMemberBasicInfo(String memberId) {
        if (memberId == null) {
            return withdrawnBasicInfo();
        }

        var basicInfoMap = fetchMemberBasicInfoByMemberIds(List.of(memberId));
        return basicInfoMap.getOrDefault(memberId, withdrawnBasicInfo());
    }

    @Override
    public Map<String, MemberExternalDTO.BasicInfo> fetchMemberBasicInfoByMemberIds(List<String> memberIds) {
        List<String> distinctMemberIds = distinctNonNullIds(memberIds);
        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }

        Map<String, MemberExternalDTO.BasicInfo> result = initializeWithdrawnBasicInfoMap(distinctMemberIds);

        List<MemberBasicInfoProjection> activeMembers =
                memberQueryService.retrieveActiveMemberBasicInfos(distinctMemberIds);

        activeMembers.forEach(projection -> result.put(projection.getId(), toBasicInfo(projection)));

        return result;
    }

    @Override
    public Map<String, DetailInfo> fetchMemberDetailInfoByMemberIds(List<String> memberIds) {
        List<String> distinctMemberIds = distinctNonNullIds(memberIds);
        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }

        Map<String, DetailInfo> result = initializeWithdrawnDetailInfoMap(distinctMemberIds);

        List<Member> members = memberQueryService.retrieveMemberById(distinctMemberIds);
        members.forEach(member -> result.put(member.getId(), toDetailInfo(member)));

        return result;
    }

    @Override
    public MemberExternalDTO.BasicInfoWithFollow fetchMemberBasicInfoWithFollow(
            String targetMemberId,
            String currentMemberId
    ) {
        if (targetMemberId == null) {
            return withdrawnBasicInfoWithFollow();
        }

        var basicInfoDTO = fetchMemberBasicInfo(targetMemberId);
        boolean isWithdrawn = isWithdrawnBasicInfo(basicInfoDTO);
        boolean isFollowing = !isWithdrawn && memberFollowQueryService.isFollowing(currentMemberId, targetMemberId);

        return MemberExternalDTO.BasicInfoWithFollow.builder()
                .nickname(basicInfoDTO.getNickname())
                .profileImageUrl(basicInfoDTO.getProfileImageUrl())
                .following(isFollowing)
                .build();
    }

    @Override
    public Map<String, MemberExternalDTO.BasicInfoWithFollow> fetchMemberBasicInfoWithFollowByMemberId(
            List<String> targetMemberIds,
            String currentMemberId
    ) {
        List<String> distinctTargetIds = distinctNonNullIds(targetMemberIds);
        if (distinctTargetIds.isEmpty()) {
            return Map.of();
        }

        Map<String, MemberExternalDTO.BasicInfo> basicInfoMap = fetchMemberBasicInfoByMemberIds(distinctTargetIds);
        Map<String, Boolean> followStatusMap =
                memberFollowQueryService.checkFollowStatusByMemberId(currentMemberId, distinctTargetIds);

        Map<String, MemberExternalDTO.BasicInfoWithFollow> result = new HashMap<>();
        for (String targetId : distinctTargetIds) {
            result.put(targetId, toBasicInfoWithFollow(targetId, basicInfoMap, followStatusMap));
        }

        return result;
    }

    @Override
    public List<String> fetchFollowingIds(String memberId) {
        return memberFollowQueryService.retrieveFollowingIds(memberId);
    }

    @Override
    public InterestCategoryInfo fetchInterestCategory(String memberId) {
        Member member = memberQueryService.retrieveMember(memberId);
        Set<MemberInterestCategory> interestCategories = member.getInterestCategories();
        List<String> categories = (interestCategories == null)
                ? List.of()
                : interestCategories.stream()
                        .filter(Objects::nonNull)
                        .map(Enum::name)
                        .sorted()
                        .toList();
        return MemberExternalDTO.InterestCategoryInfo.builder()
                .categories(categories)
                .build();
    }

    @Override
    public String fetchMemberEmail(String memberId) {
        Member member = memberQueryService.retrieveMember(memberId);
        return member.getEmail();
    }

    private MemberExternalDTO.BasicInfo withdrawnBasicInfo() {
        return MemberExternalDTO.BasicInfo.builder()
                .nickname(WITHDRAWN_MEMBER_NICKNAME)
                .profileImageUrl(null)
                .build();
    }

    private Map<String, String> initializeWithdrawnNicknameMap(List<String> memberIds) {
        Map<String, String> result = new HashMap<>();
        memberIds.forEach(memberId -> result.put(memberId, WITHDRAWN_MEMBER_NICKNAME));
        return result;
    }

    private Map<String, MemberExternalDTO.BasicInfo> initializeWithdrawnBasicInfoMap(List<String> memberIds) {
        Map<String, MemberExternalDTO.BasicInfo> result = new HashMap<>();
        memberIds.forEach(memberId -> result.put(memberId, withdrawnBasicInfo()));
        return result;
    }

    private Map<String, MemberExternalDTO.DetailInfo> initializeWithdrawnDetailInfoMap(List<String> memberIds) {
        Map<String, MemberExternalDTO.DetailInfo> result = new HashMap<>();
        memberIds.forEach(memberId -> result.put(memberId, withdrawnDetailInfo()));
        return result;
    }

    private MemberExternalDTO.BasicInfo toBasicInfo(MemberBasicInfoProjection projection) {
        return MemberExternalDTO.BasicInfo.builder()
                .nickname(projection.getNickName())
                .profileImageUrl(projection.getImgUrl())
                .build();
    }

    private MemberExternalDTO.DetailInfo toDetailInfo(Member member) {
        return MemberExternalDTO.DetailInfo.builder()
                .nickname(member.getNickName())
                .profileImageUrl(member.getImgUrl())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }

    private List<String> distinctNonNullIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> new java.util.ArrayList<>(new LinkedHashSet<>(list))));
    }

    private boolean isWithdrawnBasicInfo(MemberExternalDTO.BasicInfo basicInfo) {
        return WITHDRAWN_MEMBER_NICKNAME.equals(basicInfo.getNickname())
                && basicInfo.getProfileImageUrl() == null;
    }

    private MemberExternalDTO.BasicInfoWithFollow toBasicInfoWithFollow(
            String targetId,
            Map<String, MemberExternalDTO.BasicInfo> basicInfoMap,
            Map<String, Boolean> followStatusMap
    ) {
        MemberExternalDTO.BasicInfo basicInfo = basicInfoMap.getOrDefault(targetId, withdrawnBasicInfo());
        boolean isWithdrawn = isWithdrawnBasicInfo(basicInfo);
        boolean isFollowing = !isWithdrawn && followStatusMap.getOrDefault(targetId, false);

        return MemberExternalDTO.BasicInfoWithFollow.builder()
                .nickname(basicInfo.getNickname())
                .profileImageUrl(basicInfo.getProfileImageUrl())
                .following(isFollowing)
                .build();
    }

    private MemberExternalDTO.DetailInfo withdrawnDetailInfo() {
        return MemberExternalDTO.DetailInfo.builder()
                .nickname(WITHDRAWN_MEMBER_NICKNAME)
                .profileImageUrl(null)
                .name(null)
                .email(null)
                .build();
    }

    private MemberExternalDTO.BasicInfoWithFollow withdrawnBasicInfoWithFollow() {
        return MemberExternalDTO.BasicInfoWithFollow.builder()
                .nickname(WITHDRAWN_MEMBER_NICKNAME)
                .profileImageUrl(null)
                .following(false)
                .build();
    }
}
