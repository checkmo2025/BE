package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberSharedDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfoDTO {
        private String nickname;        // 회원 닉네임
        private String profileImageUrl; // 프로필 이미지 URL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WithFollowStatusDTO {
        private String nickname;
        private String profileImageUrl;
        private boolean isFollowing;    // 조회하는 사람 기준으로 팔로우 여부
    }
}
