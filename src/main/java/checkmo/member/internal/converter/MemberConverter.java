package checkmo.member.internal.converter;

import checkmo.member.MemberExternalDTO;
import checkmo.member.internal.entity.Member;
import checkmo.member.web.dto.MemberResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConverter {

    public static MemberResponseDTO.MemberProfileWithProfileImage toMemberProfileWithProfileImage(Member member) {
        return MemberResponseDTO.MemberProfileWithProfileImage.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    public static MemberResponseDTO.MemberProfileWithCategory toMemberProfileWithCategory(Member member) {
        return MemberResponseDTO.MemberProfileWithCategory.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(member.getInterestCategories())
                .build();
    }

    public static MemberResponseDTO.otherProfile toOtherProfile(Member member, boolean isFollowing) {
        return MemberResponseDTO.otherProfile.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(member.getInterestCategories())
                .following(isFollowing)
                .build();
    }

    public static MemberExternalDTO.WithFollowStatus toMemberProfileWithFollowStatus(
            MemberResponseDTO.MemberProfileWithFollow profile
    ) {
        return MemberExternalDTO.WithFollowStatus.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .following(profile.isFollowing())
                .build();
    }
}
