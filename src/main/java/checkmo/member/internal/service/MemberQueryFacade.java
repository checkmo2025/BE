package checkmo.member.internal.service;

import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.common.template.ExtractHelper;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Follow;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.service.query.MemberFollowQueryService;
import checkmo.member.internal.service.query.MemberQueryService;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO;
import checkmo.member.web.dto.MemberResponseDTO.BasicInfoWithFollow;
import checkmo.member.web.dto.MemberResponseDTO.DetailInfo;
import checkmo.member.web.dto.MemberResponseDTO.othersDetailInfo;
import lombok.RequiredArgsConstructor;
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
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;

    public DetailInfo retrieveMemberDetailInfo(String memberId) {
        Member member = memberQueryService.retrieveMember(memberId);

        return MemberConverter.toMemberProfileWithCategory(member);
    }

    public othersDetailInfo retrieveOthersDetailInfo(String targetMemberNickname, String memberId) {
        Member targetMember = memberQueryService.retrieveMemberByNickname(targetMemberNickname);
        boolean isFollowing = memberFollowQueryService.isFollowing(memberId, targetMember.getId());

        return MemberConverter.toOtherProfile(targetMember, isFollowing);
    }

    public MemberResponseDTO.FollowList retrieveFollowers(String memberId, Long cursorId) {
        CursorResult<Follow> followCursorResult = CursorPagingHelper.getPage(
                size -> memberFollowQueryService.retrieveFollowers(memberId, cursorId, size),
                Follow::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Follow> followerList = followCursorResult.content();
        List<String> followerIdList = ExtractHelper.extractDistinctList(followerList, follow -> follow.getFollower().getId());

        // 배치 조회 (내부 DTO)
        List<BasicInfoWithFollow> profiles = retrieveMemberBasicInfoWithFollows(followerIdList, memberId);

        return MemberResponseDTO.FollowList.builder()
                .followList(profiles)
                .hasNext(followCursorResult.hasNext())
                .nextCursor(followCursorResult.nextCursor())
                .build();
    }

    public MemberResponseDTO.FollowList retrieveFollowings(String memberId, Long cursorId) {
        CursorResult<Follow> followCursorResult = CursorPagingHelper.getPage(
                size -> memberFollowQueryService.retrieveFollowingIds(memberId, cursorId, size),
                Follow::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Follow> followingList = followCursorResult.content();

        List<String> followingIdList = ExtractHelper.extractDistinctList(followingList, follow -> follow.getFollowing().getId());

        // 배치 조회 (내부 DTO)
        List<BasicInfoWithFollow> profiles = retrieveMemberBasicInfoWithFollows(followingIdList, memberId);

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
            List<String> targetMemberIds,
            String currentMemberId
    ) {

        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 회원 기본 정보 배치 조회
        List<MemberBasicInfoProjection> memberInfoList = memberQueryService.retrieveMemberBasicInfos(
                targetMemberIds);

        // 2. 팔로우 상태 배치 조회
        Map<String, Boolean> followStatusMap = memberFollowQueryService
                .checkFollowStatusByMemberId(currentMemberId, targetMemberIds);

        // 3. 내부 DTO로 변환
        Map<String, BasicInfoWithFollow> profileMap = memberInfoList.stream()
                .collect(Collectors.toMap(
                        MemberBasicInfoProjection::getId,
                        projection -> {
                            String memberId = projection.getId();
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

    public MemberResponseDTO.LoginStatus retrieveLoginStatus(String memberId) {
        Member member = memberQueryService.retrieveMember(memberId);

        String id = member.getId();
        String provider;

        if (id.startsWith("LOCAL_")) {
            provider = "LOCAL";
        } else if (id.startsWith("KAKAO_")) {
            provider = "KAKAO";
        } else if (id.startsWith("GOOGLE_")) {
            provider = "GOOGLE";
        } else if (id.startsWith("NAVER_")) {
            provider = "NAVER";
        } else {
            provider = "SOCIAL";
        }

        return MemberResponseDTO.LoginStatus.builder()
                                            .provider(provider)
                                            .email(member.getEmail())
                                            .build();
    }
}