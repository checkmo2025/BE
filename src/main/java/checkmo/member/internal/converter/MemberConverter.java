package checkmo.member.internal.converter;

import checkmo.member.MemberExternalDTO;
import checkmo.member.internal.entity.Follow;
import checkmo.member.internal.entity.Member;
import checkmo.member.web.dto.MemberResponseDTO;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConverter {

    // =====================================================
    // Entity ↔ DTO 변환
    // =====================================================

    /**
     * Member 엔티티 → MemberProfileResponseDTO 변환
     */
    public static MemberResponseDTO.MemberProfileWithProfileImage toMemberProfileWithProfileImage(Member member) {
        return MemberResponseDTO.MemberProfileWithProfileImage.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    /**
     * Member 엔티티 → MemberProfileWithCategoryResponseDTO 변환
     */
    public static MemberResponseDTO.MemberProfileWithCategory toMemberProfileWithCategory(Member member) {
        return MemberResponseDTO.MemberProfileWithCategory.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(member.getInterestCategories())
                .build();
    }

    /**
     * MemberProfileResponseDTO → BasicInfoDTO 변환
     */
    public static MemberExternalDTO.BasicInfo toBasicInfo(MemberResponseDTO.MemberProfileWithProfileImage profile) {
        return MemberExternalDTO.BasicInfo.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .build();
    }

    /**
     * BasicInfoDTO -> WithFollowStatusDTO 변환
     */
    public static MemberExternalDTO.WithFollowStatus toWithFollowStatus(
            MemberExternalDTO.BasicInfo basicInfo,
            boolean isFollowing
    ) {
        return MemberExternalDTO.WithFollowStatus.builder()
                .nickname(basicInfo.getNickname())
                .profileImageUrl(basicInfo.getProfileImageUrl())
                .following(isFollowing)
                .build();
    }

    public static MemberResponseDTO.otherProfileResponse toOtherProfileResponse(
            Member member,
            boolean isFollowing
    ) {
        return MemberResponseDTO.otherProfileResponse.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(member.getInterestCategories())
                .following(isFollowing)
                .build();
    }

    // =====================================================
    // DTO ↔ Entity 변환
    // =====================================================

    /**
     * follower, Following → Follow 엔티티 변환
     */
    public static Follow toFollow(Member follower, Member following) {
        return Follow.builder()
                .follower(follower)
                .following(following)
                .build();
    }

    // =====================================================
    // DTO ↔ DTO 변환
    // =====================================================

    /**
     * follow -> MemberResponseDTO.FollowList 변환
     */
    public static MemberResponseDTO.FollowList toFollowList(
            List<MemberResponseDTO.MemberProfileWithFollow> followList,
            boolean hasNext,
            Long nextCursor
    ) {
        return MemberResponseDTO.FollowList.builder()
                .followList(followList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    // =====================================================
    // 내부 DTO 변환
    // =====================================================

    public static MemberResponseDTO.MemberProfileWithFollow toMemberProfile(String nickname, String profileImageUrl, boolean isFollowing) {
        return MemberResponseDTO.MemberProfileWithFollow.builder()
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .isFollowing(isFollowing)
                .build();
    }

    public static MemberExternalDTO.WithFollowStatus toMemberExternalDTOWithFollowStatus(MemberResponseDTO.MemberProfileWithFollow profile) {
        return MemberExternalDTO.WithFollowStatus.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .following(profile.isFollowing())
                .build();
    }

    public static Member toMember(String memberId, String email) {
        return Member.builder()
                .id(memberId)
                .email(email)
                .nickName("")
                .description("")
                .imgUrl(null)
                .build();
    }
}
