package checkmo.member.internal.service;

import checkmo.authentication.AuthenticationAPI;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.common.template.ExtractHelper;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.converter.TermsConverter;
import checkmo.member.internal.entity.Follow;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberBlock;
import checkmo.member.internal.entity.MemberInterestCategory;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.service.query.MemberBlockQueryService;
import checkmo.member.internal.service.query.MemberFollowQueryService;
import checkmo.member.internal.service.query.MemberQueryService;
import checkmo.member.internal.service.query.MemberTermsQueryService;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO;
import checkmo.member.web.dto.MemberResponseDTO.*;
import checkmo.member.web.dto.TermsResponseDTO.MemberTermsStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberQueryFacade {

    // 페이징 기본 크기 상수
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int RECOMMENDED_MEMBER_LIMIT = 4;
    private static final int ADMIN_PAGE_SIZE = 12;

    private final AuthenticationAPI authenticationAPI;

    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;
    private final MemberBlockQueryService memberBlockQueryService;
    private final MemberTermsQueryService memberTermsQueryService;

    public DetailInfo retrieveMemberDetailInfo(Long memberId) {
        Member member = memberQueryService.retrieveMember(memberId);

        return MemberConverter.toMemberProfileWithCategory(member, isSocialMember(memberId));
    }

    public MemberTermsStatus retrieveMemberTermsStatus(Long memberId) {
        List<Terms> activeTerms = memberTermsQueryService.retrieveActiveTerms();

        return TermsConverter.toMemberTermsStatus(
                activeTerms,
                memberTermsQueryService.retrieveLatestMemberTermsByTermsId(
                        memberId,
                        activeTerms.stream()
                                .map(Terms::getId)
                                .toList()
                )
        );
    }

    public MemberResponseDTO.FollowCount retrieveMyFollowCount(Long memberId) {
        long followerCount = memberFollowQueryService.countFollowers(memberId);
        long followingCount = memberFollowQueryService.countFollowings(memberId);

        return MemberResponseDTO.FollowCount.builder()
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    public othersDetailInfo retrieveOthersDetailInfo(String targetMemberNickname, Long memberId) {
        Member targetMember = memberQueryService.retrieveMemberByNickname(targetMemberNickname);
        memberBlockQueryService.validateProfileAccessible(memberId, targetMember.getId());

        boolean isFollowing = memberFollowQueryService.isFollowing(memberId, targetMember.getId());
        long followerCount = memberFollowQueryService.countFollowers(targetMember.getId());
        long followingCount = memberFollowQueryService.countFollowings(targetMember.getId());

        return MemberConverter.toOtherProfile(
                targetMember,
                isFollowing,
                followerCount,
                followingCount
        );
    }

    public MemberResponseDTO.FollowList retrieveFollowers(Long memberId, Long cursorId) {
        return retrieveFollowers(memberId, memberId, cursorId);
    }

    public MemberResponseDTO.FollowList retrieveOtherFollowers(String targetMemberNickname, Long currentMemberId, Long cursorId) {
        Member targetMember = memberQueryService.retrieveMemberByNickname(targetMemberNickname);
        memberBlockQueryService.validateProfileAccessible(currentMemberId, targetMember.getId());
        return retrieveFollowers(targetMember.getId(), currentMemberId, cursorId);
    }

    private MemberResponseDTO.FollowList retrieveFollowers(Long targetMemberId, Long currentMemberId, Long cursorId) {
        List<Long> blockRelatedMemberIds = memberBlockQueryService.retrieveBlockRelatedMemberIds(currentMemberId);
        CursorResult<Follow> followCursorResult = CursorPagingHelper.getPage(
                size -> memberFollowQueryService.retrieveFollowers(targetMemberId, cursorId, size, blockRelatedMemberIds),
                Follow::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Follow> followerList = followCursorResult.content();
        List<Long> followerIdList = ExtractHelper.extractDistinctList(followerList, follow -> follow.getFollower().getId());

        // 배치 조회 (내부 DTO)
        List<BasicInfoWithFollow> profiles = retrieveMemberBasicInfoWithFollows(followerIdList, currentMemberId);

        return MemberResponseDTO.FollowList.builder()
                .followList(profiles)
                .hasNext(followCursorResult.hasNext())
                .nextCursor(followCursorResult.nextCursor())
                .build();
    }

    public MemberResponseDTO.FollowList retrieveFollowings(Long memberId, Long cursorId) {
        return retrieveFollowings(memberId, memberId, cursorId);
    }

    public MemberResponseDTO.FollowList retrieveOtherFollowings(String targetMemberNickname, Long currentMemberId, Long cursorId) {
        Member targetMember = memberQueryService.retrieveMemberByNickname(targetMemberNickname);
        memberBlockQueryService.validateProfileAccessible(currentMemberId, targetMember.getId());
        return retrieveFollowings(targetMember.getId(), currentMemberId, cursorId);
    }

    private MemberResponseDTO.FollowList retrieveFollowings(Long targetMemberId, Long currentMemberId, Long cursorId) {
        List<Long> blockRelatedMemberIds = memberBlockQueryService.retrieveBlockRelatedMemberIds(currentMemberId);
        CursorResult<Follow> followCursorResult = CursorPagingHelper.getPage(
                size -> memberFollowQueryService.retrieveFollowingIds(targetMemberId, cursorId, size, blockRelatedMemberIds),
                Follow::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Follow> followingList = followCursorResult.content();

        List<Long> followingIdList = ExtractHelper.extractDistinctList(followingList, follow -> follow.getFollowing().getId());

        // 배치 조회 (내부 DTO)
        List<BasicInfoWithFollow> profiles = retrieveMemberBasicInfoWithFollows(followingIdList, currentMemberId);

        return MemberResponseDTO.FollowList.builder()
                .followList(profiles)
                .hasNext(followCursorResult.hasNext())
                .nextCursor(followCursorResult.nextCursor())
                .build();
    }

    /**
     * 회원 ID 목록에 대한 프로필 + 팔로우 상태를 배치 조회 (내부용) MemberQueryFacade 내부에서 사용하며, MemberAPI에서도 재사용 가능
     *
     * @param targetMemberIds 조회할 회원 ID 목록
     * @param currentMemberId 현재 회원 ID (팔로우 상태 확인용)
     * @return 회원 프로필 목록 (내부 DTO)
     */
    public List<BasicInfoWithFollow> retrieveMemberBasicInfoWithFollows(
            List<Long> targetMemberIds,
            Long currentMemberId
    ) {

        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 활성 회원 기본 정보 배치 조회
        List<MemberBasicInfoProjection> memberInfoList = memberQueryService.retrieveActiveMemberBasicInfos(
                targetMemberIds);

        // 2. 팔로우 상태 배치 조회
        Map<Long, Boolean> followStatusMap = memberFollowQueryService
                .checkFollowStatusByMemberId(currentMemberId, targetMemberIds);

        // 3. 내부 DTO로 변환
        Map<Long, BasicInfoWithFollow> profileMap = memberInfoList.stream()
                .collect(Collectors.toMap(
                        MemberBasicInfoProjection::getId,
                        projection -> {
                            Long memberId = projection.getId();
                            boolean isFollowing = followStatusMap.getOrDefault(memberId, false);
                            return BasicInfoWithFollow.builder()
                                    .nickname(projection.getNickName())
                                    .profileImageUrl(projection.getImgUrl())
                                    .isFollowing(isFollowing)
                                    .build();
                        }
                ));

        // 4. 순서 유지하며 반환
        return targetMemberIds.stream()
                .map(profileMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public MemberResponseDTO.FindEmailResult retrieveMemberEmail(MemberRequestDTO.FindEmail request) {
        String email = memberQueryService.retrieveMemberEmail(request);
        String maskedEmail = maskEmail(email);
        return MemberResponseDTO.FindEmailResult.builder()
                .email(maskedEmail)
                .build();
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex < 0) return email;
        String id = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (id.length() <= 4) return "****" + domain;
        return id.substring(0, id.length() - 4) + "****" + domain;
    }

    public RecommendedMemberList retrieveRecommendedMembers(Long memberId) {
        Member member = memberQueryService.retrieveMember(memberId);
        List<MemberInterestCategory> myInterests = List.copyOf(member.getInterestCategories());
        List<Long> blockRelatedMemberIds = memberBlockQueryService.retrieveBlockRelatedMemberIds(memberId);

        List<Member> recommendedMembers = memberQueryService.retrieveRecommendedMembers(
                memberId, myInterests, blockRelatedMemberIds, RECOMMENDED_MEMBER_LIMIT
        );

        List<RecommendedMember> friends = recommendedMembers.stream()
                .map(MemberConverter::toRecommendedMember)
                .toList();

        return RecommendedMemberList.builder()
                .friends(friends)
                .build();
    }

    public BlockedMemberList retrieveBlockedMembers(Long memberId, Long cursorId) {
        CursorResult<MemberBlock> blockCursorResult = CursorPagingHelper.getPage(
                size -> memberBlockQueryService.retrieveBlocks(memberId, cursorId, size),
                MemberBlock::getId,
                DEFAULT_PAGE_SIZE
        );

        List<BlockedMember> blocks = blockCursorResult.content().stream()
                .map(MemberConverter::toBlockedMember)
                .toList();

        return BlockedMemberList.builder()
                .blocks(blocks)
                .hasNext(blockCursorResult.hasNext())
                .nextCursor(blockCursorResult.nextCursor())
                .build();
    }

    public MemberResponseDTO.LoginStatus retrieveLoginStatus(Long memberId) {
        Member member = memberQueryService.retrieveMember(memberId);

        String authProvider = authenticationAPI.fetchProvider(memberId);
        String provider = switch (authProvider) {
            case "LOCAL", "KAKAO", "GOOGLE", "NAVER", "APPLE" -> authProvider;
            default -> "SOCIAL";
        };

        boolean isAdmin = authenticationAPI.canAccessAdmin(memberId);

        return MemberResponseDTO.LoginStatus.builder()
                .provider(provider)
                .email(member.getEmail())
                .admin(isAdmin)
                .build();
    }

    public MemberResponseDTO.MemberEmailList retrieveActiveEmailsForAdmin(String keyword, int limit) {
        List<String> emails = memberQueryService.retrieveActiveEmailsByKeyword(keyword, limit);
        return MemberResponseDTO.MemberEmailList.builder()
                .emails(emails)
                .build();
    }


    /**
     * 관리자 전용 회원 목록 조회
     */
    public MemberResponseDTO.AdminMemberList retrieveMembersForAdmin(String keyword, int page) {
        int safePage = Math.max(page, 1);

        Page<Member> memberPage = memberQueryService.retrieveMembersForAdmin(
                keyword,
                safePage - 1,
                ADMIN_PAGE_SIZE
        );

        List<MemberResponseDTO.AdminBasicInfo> memberList = memberPage.getContent().stream()
                .map(member -> MemberResponseDTO.AdminBasicInfo.builder()
                        .memberId(String.valueOf(member.getId()))
                        .nickname(member.getNickName())
                        .name(member.getName())
                        .email(member.getEmail())
                        .phoneNumber(member.getPhoneNumber())
                        .build())
                .toList();

        return MemberResponseDTO.AdminMemberList.builder()
                .memberList(memberList)
                .page(safePage)
                .pageSize(memberPage.getSize())
                .totalPages(memberPage.getTotalPages())
                .totalElements(memberPage.getTotalElements())
                .hasNext(memberPage.hasNext())
                .build();
    }

    /**
     * 관리자 전용 회원 기본 상세 조회
     * 현재 컨트롤러 기준: 닉네임으로 조회
     */
    public MemberResponseDTO.AdminMemberDetailInfo retrieveMemberDetailInfoForAdmin(String memberNickName) {
        Member member = memberQueryService.retrieveMemberByNickname(memberNickName);

        return MemberResponseDTO.AdminMemberDetailInfo.builder()
                .memberId(String.valueOf(member.getId()))
                .nickname(member.getNickName())
                .name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(member.getInterestCategories())
                .active(member.isActive())
                .build();
    }

    private boolean isSocialMember(Long memberId) {
        return !"LOCAL".equals(authenticationAPI.fetchProvider(memberId));
    }
}
