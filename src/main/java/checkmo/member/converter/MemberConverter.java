package checkmo.member.converter;

import checkmo.member.entity.Follow;
import checkmo.member.entity.Member;
import checkmo.member.entity.MemberCategory;
import checkmo.member.service.security.oauth2.OAuth2Attributes;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO;
import checkmo.global.dto.CategorySharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import java.util.List;
import java.util.UUID;
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
        String tempNickname = "TEMP_" + newMemberId; // 닉넴 임시로 일단 넣기

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
     * OAuth2 소셜 로그인 → Member 엔티티 변환
     */
    public static Member fromOAuth2Attributes(OAuth2Attributes attributes, String registrationId) {
        String newMemberId = registrationId.toUpperCase() + "_" + attributes.getProviderId();
        String tempNickname = "TEMP_" + newMemberId; // 닉넴 임시로 일단 넣기

        return Member.builder()
                        .id(newMemberId)
                        .email(attributes.getEmail())
                        .password("") // OAuth2 사용자는 비밀번호가 없음
                        .nickName(tempNickname)
                        .description("")
                        .role(Member.Role.USER) // 기본 역할 설정
                        .deactivated(null)
                        .isProfileCompleted(false) // 프로필 미완료 상태로 설정
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
     * Member 엔티티 → MemberResponseDTO.FollowResponse 변환
     */
    public static MemberResponseDTO.MemberProfileWithCategoryResponseDTO toMemberProfileWithCategoryResponseDTO(Member member, List<CategorySharedDTO.CategoryInfo> categories) {
        return MemberResponseDTO.MemberProfileWithCategoryResponseDTO.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(categories)
                .build();
    }
    /**
     * MemberProfileResponseDTO → BasicInfoDTO 변환
     */
    public static MemberSharedDTO.BasicInfo toBasicInfoDTO(MemberResponseDTO.MemberProfileResponseDTO profile) {
        return MemberSharedDTO.BasicInfo.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .build();
    }

    /**
     * BasicInfoDTO -> WithFollowStatusDTO 변환
     */
    public static MemberSharedDTO.WithFollowStatus toWithFollowStatusDTO(MemberSharedDTO.BasicInfo basicInfo, boolean isFollowing) {
        return MemberSharedDTO.WithFollowStatus.builder()
                .nickname(basicInfo.getNickname())
                .profileImageUrl(basicInfo.getProfileImageUrl())
                .following(isFollowing)
                .build();
    }

    /**
     * Object[] -> WithFollowStatusDTO 변환 (공유용)
     * 배치 처리를 위한 조회 결과를 Object[]에 담아서 전달
     * 여기서 Object[]의 구성은 row[0]=memberId, row[1]=nickname, row[2]=profileImageUrl
     */
    public static MemberSharedDTO.WithFollowStatus toWithFollowStatusDTO(Object[] row, boolean isFollowing) {
        return MemberSharedDTO.WithFollowStatus.builder()
                .nickname((String) row[1])
                .profileImageUrl((String) row[2])
                .following(isFollowing)
                .build();
    }

    public static MemberResponseDTO.otherProfileResponseDTO toOtherProfileResponseDTO(Member member, boolean isFollowing, List<CategorySharedDTO.CategoryInfo> categories) {
        return MemberResponseDTO.otherProfileResponseDTO.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(categories)
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
            List<MemberSharedDTO.WithFollowStatus> followList,
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
            List<MemberSharedDTO.WithFollowStatus> followList
    ) {
        return MemberResponseDTO.FollowPreviewList.builder()
                .followList(followList)
                .build();
    }

    // =====================================================
    // MemberCategory <-> DTO 변환
    // =====================================================

    /**
     * List<MemberCategory> → CategoryInfoList 변환
     */
    public static List<CategorySharedDTO.CategoryInfo> fromMemberCategoriesToCategoryInfoList(List<MemberCategory> memberCategories) {
        return memberCategories.stream()
                .map(mc -> CategorySharedDTO.CategoryInfo.builder()
                        .id(mc.getCategory().getId())
                        .name(mc.getCategory().getName())
                        .build())
                .toList();
    }
}
