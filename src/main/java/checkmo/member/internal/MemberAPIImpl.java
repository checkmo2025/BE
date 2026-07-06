package checkmo.member.internal;

import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO;
import checkmo.member.MemberExternalDTO.DetailInfo;
import checkmo.member.MemberExternalDTO.InterestCategoryInfo;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberInterestCategory;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.service.query.MemberBlockQueryService;
import checkmo.member.internal.service.query.MemberFollowQueryService;
import checkmo.member.internal.service.query.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAPIImpl implements MemberAPI {

    private static final String WITHDRAWN_MEMBER_NICKNAME = "탈퇴한 회원";

    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;
    private final MemberBlockQueryService memberBlockQueryService;

    @Override
    public Long fetchMemberId(String nickname) {
        return memberQueryService.retrieveMemberId(nickname);
    }

    @Override
    public String fetchNickname(Long memberId) {
        if (memberId == null) {
            return WITHDRAWN_MEMBER_NICKNAME;
        }

        return memberQueryService.retrieveActiveMemberNickname(memberId)
                .orElse(WITHDRAWN_MEMBER_NICKNAME);
    }

    @Override
    public Map<Long, String> fetchNicknameByMemberIds(List<Long> memberIds) {
        List<Long> distinctMemberIds = distinctNonNullIds(memberIds);
        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> result = initializeWithdrawnNicknameMap(distinctMemberIds);

        memberQueryService.retrieveActiveMemberNicknameByMemberIds(distinctMemberIds)
                .forEach(result::put);
        return result;
    }

    @Override
    public MemberExternalDTO.BasicInfo fetchMemberBasicInfo(Long memberId) {
        if (memberId == null) {
            return withdrawnBasicInfo();
        }

        var basicInfoMap = fetchMemberBasicInfoByMemberIds(List.of(memberId));
        return basicInfoMap.getOrDefault(memberId, withdrawnBasicInfo());
    }

    @Override
    public Map<Long, MemberExternalDTO.BasicInfo> fetchMemberBasicInfoByMemberIds(List<Long> memberIds) {
        List<Long> distinctMemberIds = distinctNonNullIds(memberIds);
        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, MemberExternalDTO.BasicInfo> result = initializeWithdrawnBasicInfoMap(distinctMemberIds);

        List<MemberBasicInfoProjection> activeMembers =
                memberQueryService.retrieveActiveMemberBasicInfos(distinctMemberIds);

        activeMembers.forEach(projection -> result.put(projection.getId(), toBasicInfo(projection)));

        return result;
    }

    @Override
    public Map<Long, DetailInfo> fetchMemberDetailInfoByMemberIds(List<Long> memberIds) {
        List<Long> distinctMemberIds = distinctNonNullIds(memberIds);
        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, DetailInfo> result = initializeWithdrawnDetailInfoMap(distinctMemberIds);

        List<Member> members = memberQueryService.retrieveMemberById(distinctMemberIds);
        members.forEach(member -> result.put(member.getId(), toDetailInfo(member)));

        return result;
    }

    @Override
    public Map<Long, MemberExternalDTO.PersonalInfo> fetchMemberPersonalInfoByMemberIds(List<Long> memberIds) {
        List<Long> distinctMemberIds = distinctNonNullIds(memberIds);
        if (distinctMemberIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, MemberExternalDTO.PersonalInfo> result = initializeWithdrawnPersonalInfoMap(distinctMemberIds);

        List<Member> members = memberQueryService.retrieveMemberById(distinctMemberIds);
        members.forEach(member -> result.put(member.getId(), toPersonalInfo(member)));

        return result;
    }

    @Override
    public MemberExternalDTO.BasicInfoWithFollow fetchMemberBasicInfoWithFollow(
            Long targetMemberId,
            Long currentMemberId
    ) {
        if (targetMemberId == null) {
            return withdrawnBasicInfoWithFollow();
        }

        var basicInfoDTO = fetchMemberBasicInfo(targetMemberId);
        boolean isWithdrawn = isWithdrawnBasicInfo(basicInfoDTO);
        boolean isFollowing = !isWithdrawn && memberFollowQueryService.isFollowing(
                currentMemberId,
                targetMemberId
        );

        return MemberExternalDTO.BasicInfoWithFollow.builder()
                .nickname(basicInfoDTO.getNickname())
                .profileImageUrl(basicInfoDTO.getProfileImageUrl())
                .following(isFollowing)
                .build();
    }

    @Override
    public Map<Long, MemberExternalDTO.BasicInfoWithFollow> fetchMemberBasicInfoWithFollowByMemberId(
            List<Long> targetMemberIds,
            Long currentMemberId
    ) {
        List<Long> distinctTargetIds = distinctNonNullIds(targetMemberIds);
        if (distinctTargetIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, MemberExternalDTO.BasicInfo> basicInfoMap = fetchMemberBasicInfoByMemberIds(distinctTargetIds);
        Map<Long, Boolean> followStatusMap =
                memberFollowQueryService.checkFollowStatusByMemberId(
                        currentMemberId,
                        distinctTargetIds
                );

        Map<Long, MemberExternalDTO.BasicInfoWithFollow> result = new HashMap<>();
        for (Long targetId : distinctTargetIds) {
            result.put(targetId, toBasicInfoWithFollow(targetId, basicInfoMap, followStatusMap));
        }

        return result;
    }

    @Override
    public List<Long> fetchFollowingIds(Long memberId) {
        return memberFollowQueryService.retrieveFollowingIds(memberId);
    }

    @Override
    public List<Long> fetchBlockedMemberIds(Long blockerId) {
        return memberBlockQueryService.retrieveBlockedMemberIds(blockerId);
    }

    @Override
    public List<Long> fetchBlockRelatedMemberIds(Long memberId) {
        return memberBlockQueryService.retrieveBlockRelatedMemberIds(memberId);
    }

    @Override
    public boolean hasBlockBetween(Long memberId1, Long memberId2) {
        return memberBlockQueryService.hasBlockBetween(memberId1, memberId2);
    }

    @Override
    public boolean hasBlocked(Long blockerId, Long blockedId) {
        return memberBlockQueryService.hasBlocked(blockerId, blockedId);
    }

    @Override
    public void validateProfileAccessible(Long viewerId, Long targetMemberId) {
        memberBlockQueryService.validateProfileAccessible(viewerId, targetMemberId);
    }

    @Override
    public InterestCategoryInfo fetchInterestCategory(Long memberId) {
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
    public String fetchMemberEmail(Long memberId) {
        Member member = memberQueryService.retrieveMember(memberId);
        return member.getEmail();
    }

    private MemberExternalDTO.BasicInfo withdrawnBasicInfo() {
        return MemberExternalDTO.BasicInfo.builder()
                .nickname(WITHDRAWN_MEMBER_NICKNAME)
                .profileImageUrl(null)
                .build();
    }

    private Map<Long, String> initializeWithdrawnNicknameMap(List<Long> memberIds) {
        Map<Long, String> result = new HashMap<>();
        memberIds.forEach(memberId -> result.put(memberId, WITHDRAWN_MEMBER_NICKNAME));
        return result;
    }

    private Map<Long, MemberExternalDTO.PersonalInfo> initializeWithdrawnPersonalInfoMap(List<Long> memberIds) {
        Map<Long, MemberExternalDTO.PersonalInfo> result = new HashMap<>();
        memberIds.forEach(memberId -> result.put(memberId, withdrawnPersonalInfo()));
        return result;
    }

    private Map<Long, MemberExternalDTO.BasicInfo> initializeWithdrawnBasicInfoMap(List<Long> memberIds) {
        Map<Long, MemberExternalDTO.BasicInfo> result = new HashMap<>();
        memberIds.forEach(memberId -> result.put(memberId, withdrawnBasicInfo()));
        return result;
    }

    private Map<Long, MemberExternalDTO.DetailInfo> initializeWithdrawnDetailInfoMap(List<Long> memberIds) {
        Map<Long, MemberExternalDTO.DetailInfo> result = new HashMap<>();
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

    private MemberExternalDTO.PersonalInfo toPersonalInfo(Member member) {
        return MemberExternalDTO.PersonalInfo.builder()
                .nickname(member.getNickName())
                .name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .build();
    }

    private List<Long> distinctNonNullIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> new ArrayList<>(new LinkedHashSet<>(list))));
    }

    private boolean isWithdrawnBasicInfo(MemberExternalDTO.BasicInfo basicInfo) {
        return WITHDRAWN_MEMBER_NICKNAME.equals(basicInfo.getNickname())
                && basicInfo.getProfileImageUrl() == null;
    }

    private MemberExternalDTO.BasicInfoWithFollow toBasicInfoWithFollow(
            Long targetId,
            Map<Long, MemberExternalDTO.BasicInfo> basicInfoMap,
            Map<Long, Boolean> followStatusMap
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

    private MemberExternalDTO.PersonalInfo withdrawnPersonalInfo() {
        return MemberExternalDTO.PersonalInfo.builder()
                .nickname(WITHDRAWN_MEMBER_NICKNAME)
                .name(null)
                .email(null)
                .phoneNumber(null)
                .build();
    }
}
