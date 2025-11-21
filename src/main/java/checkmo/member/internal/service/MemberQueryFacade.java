package checkmo.member.internal.service;

import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.internal.converter.MemberConverter;
import checkmo.member.internal.entity.Follow;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.service.query.MemberFollowQueryService;
import checkmo.member.internal.service.query.MemberQueryService;
import checkmo.member.web.dto.MemberResponseDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryFacade {

    // 페이징 기본 크기 상수
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final MemberQueryService memberQueryService;
    private final MemberFollowQueryService memberFollowQueryService;

    public MemberResponseDTO.MemberProfileWithCategory getMemberProfile(String memberId) {
        Member member = memberQueryService.getMemberProfile(memberId);

        return MemberConverter.toMemberProfileWithCategory(member);
    }

    public MemberResponseDTO.otherProfile getOtherProfile(String targetMemberNickname, String memberId) {
        Member targetMember = memberQueryService.getOtherProfile(targetMemberNickname);
        boolean isFollowing = memberFollowQueryService.isFollowing(memberId, targetMember.getId());

        return MemberConverter.toOtherProfile(targetMember, isFollowing);
    }

    public MemberResponseDTO.FollowList getFollowerList(String memberId, Long cursorId) {
        CursorResult<Follow> followCursorResult = CursorPagingHelper.getPage(
                size -> memberFollowQueryService.getFollowerList(memberId, cursorId, size),
                Follow::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Follow> followerList = followCursorResult.content();
        List<String> followerIdList = extractFollowerMemberIds(followerList);

        // 배치 조회 (내부 DTO)
        List<MemberResponseDTO.MemberProfileWithFollow> profiles = getMemberProfiles(followerIdList, memberId);

        return MemberResponseDTO.FollowList.builder()
                .followList(profiles)
                .hasNext(followCursorResult.hasNext())
                .nextCursor(followCursorResult.nextCursor())
                .build();
    }

    public MemberResponseDTO.FollowList getFollowingList(String memberId, Long cursorId) {
        CursorResult<Follow> followCursorResult = CursorPagingHelper.getPage(
                size -> memberFollowQueryService.getFollowingList(memberId, cursorId, size),
                Follow::getId,
                DEFAULT_PAGE_SIZE
        );
        List<Follow> followingList = followCursorResult.content();

        List<String> followingIdList = extractFollowingMemberIds(followingList);

        // 배치 조회 (내부 DTO)
        List<MemberResponseDTO.MemberProfileWithFollow> profiles = getMemberProfiles(followingIdList, memberId);

        return MemberResponseDTO.FollowList.builder()
                .followList(profiles)
                .hasNext(followCursorResult.hasNext())
                .nextCursor(followCursorResult.nextCursor())
                .build();
    }

    private List<String> extractFollowingMemberIds(List<Follow> followingList) {
        return followingList.stream()
                .map(Follow::getFollowingId)
                .distinct()
                .toList();
    }

    private List<String> extractFollowerMemberIds(List<Follow> followerList) {
        return followerList.stream()
                .map(Follow::getFollowerId)
                .distinct()
                .toList();
    }

    /**
     * 회원 ID 목록에 대한 프로필 + 팔로우 상태를 배치 조회 (내부용) MemberQueryFacade 내부에서 사용하며, MemberAPI에서도 재사용 가능
     *
     * @param targetMemberIds 조회할 회원 ID 목록
     * @param currentMemberId 현재 회원 ID (팔로우 상태 확인용)
     * @return 회원 프로필 목록 (내부 DTO)
     */
    public List<MemberResponseDTO.MemberProfileWithFollow> getMemberProfiles(
            List<String> targetMemberIds,
            String currentMemberId
    ) {

        if (targetMemberIds == null || targetMemberIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 회원 기본 정보 배치 조회
        List<MemberBasicInfoProjection> memberInfoList = memberQueryService.getMemberNicknamesAndProfileImagesByMemberIds(
                targetMemberIds);

        // 2. 팔로우 상태 배치 조회
        Map<String, Boolean> followStatusMap = memberFollowQueryService
                .getFollowStatusMapForMembers(currentMemberId, targetMemberIds);

        // 3. 내부 DTO로 변환
        Map<String, MemberResponseDTO.MemberProfileWithFollow> profileMap = memberInfoList.stream()
                .collect(Collectors.toMap(
                        MemberBasicInfoProjection::getId,
                        projection -> {
                            String memberId = projection.getId();
                            boolean isFollowing = followStatusMap.getOrDefault(memberId, false);
                            return MemberResponseDTO.MemberProfileWithFollow.builder()
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
}
