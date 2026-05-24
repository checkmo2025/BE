package checkmo.member.internal.converter;

import checkmo.member.MemberExternalDTO;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberBlock;
import checkmo.member.web.dto.MemberResponseDTO.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConverter {

    public static BasicInfoWithDescription toMemberProfileWithProfileImage(Member member) {
        return BasicInfoWithDescription.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    public static DetailInfo toMemberProfileWithCategory(Member member) {
        return DetailInfo.builder()
                .nickname(member.getNickName())
                .name(member.getName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .phoneNumber(member.getPhoneNumber())
                .categories(member.getInterestCategories())
                .build();
    }

    public static othersDetailInfo toOtherProfile(
            Member member,
            boolean isFollowing,
            long followerCount,
            long followingCount
    ) {
        return othersDetailInfo.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .following(isFollowing)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    public static MemberExternalDTO.BasicInfoWithFollow toMemberProfileWithFollowStatus(
            BasicInfoWithFollow profile
    ) {
        return MemberExternalDTO.BasicInfoWithFollow.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .following(profile.isFollowing())
                .build();
    }

    public static RecommendedMember toRecommendedMember(Member member) {
        return RecommendedMember.builder()
                .nickname(member.getNickName())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    public static BlockedMember toBlockedMember(MemberBlock memberBlock) {
        Member blocked = memberBlock.getBlocked();
        return BlockedMember.builder()
                .memberId(blocked.getId())
                .nickname(blocked.getNickName())
                .profileImageUrl(blocked.getImgUrl())
                .build();
    }

}
