package checkmo.domain.member.converter;

import checkmo.domain.member.entity.Follow;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;

import java.util.List;
import java.util.UUID;
import checkmo.global.dto.MemberSharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConverter {

    // =====================================================
    // Entity ↔ DTO 변환
    // =====================================================

    /**
     * Member 엔티티 → MemberSignUpResponseDTO 변환
     */
    public static MemberResponseDTO.SignUpResponseDTO fromMember(Member member) {
        return MemberResponseDTO.SignUpResponseDTO.builder()
                                                  .email(member.getEmail())
                                                  .isProfileCompleted(member.isProfileCompleted())
                                                  .build();
    }

    /**
     * SignUpRequestDTO → Member 엔티티 변환
     */
    public static Member fromSignUpRequestDTO(MemberRequestDTO.SignUpRequestDTO request,
                                              String encodedPassword) {

        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String newMemberId = "LOCAL_" + uuid;
        String tempNickname = "TEMP_" + uuid; // 닉넴 임시로 일단 넣기

        return Member.builder()
                     .id(newMemberId)
                     .email(request.getEmail())
                     .password(encodedPassword)
                     .nickName(tempNickname)
                     .description("")
                     .role(Member.Role.USER)
                     .deactivated(null)
                     .isProfileCompleted(false)
                     .build();
    }

    /**
     * Member 엔티티 → MemberLoginResponseDTO 변환
     */
    public static MemberResponseDTO.LoginResponseDTO fromMemberToLoginResponseDTO(Member member) {
        return MemberResponseDTO.LoginResponseDTO.builder()
                .nickname(member.getNickName())
                .build();
    }

    /**
     * Member 엔티티 → MemberProfileResponseDTO 변환
     */
    public static MemberResponseDTO.MemberProfileResponseDTO toMemberProfileResponseDTO(Member member) {
        return MemberResponseDTO.MemberProfileResponseDTO.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    /**
     * MemberProfileResponseDTO → BasicInfoDTO 변환
     */
    public static MemberSharedDTO.BasicInfoDTO toBasicInfoDTO(MemberResponseDTO.MemberProfileResponseDTO profile) {
        return MemberSharedDTO.BasicInfoDTO.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .build();
    }

    /**
     * BasicInfoDTO -> WithFollowStatusDTO 변환
     */
    public static MemberSharedDTO.WithFollowStatusDTO toWithFollowStatusDTO(MemberSharedDTO.BasicInfoDTO basicInfo, boolean isFollowing) {
        return MemberSharedDTO.WithFollowStatusDTO.builder()
                .nickname(basicInfo.getNickname())
                .profileImageUrl(basicInfo.getProfileImageUrl())
                .isFollowing(isFollowing)
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
            List<MemberResponseDTO.FollowResponse> followList,
            boolean hasNext,
            Long nextCursor
    ) {
        return MemberResponseDTO.FollowList.builder()
                .followList(followList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * follow -> MemberResponseDTO.FollowPreviewList 변환
     */
    public static MemberResponseDTO.FollowPreviewList toFollowPreviewList(
            List<MemberResponseDTO.FollowResponse> followList
    ) {
        return MemberResponseDTO.FollowPreviewList.builder()
                .followList(followList)
                .build();
    }
}
