package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 도메인과 관련된 공유 DTO 클래스
 * 다른 도메인에서 회원 정보를 참조할 때 사용
 */
public class MemberSharedDTO {

    /**
     * 회원 기본 정보 DTO
     * 회원의 핵심 정보(닉네임, 프로필 이미지)를 포함
     * 외부 도메인에서 회원 정보를 간략히 참조할 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfoDTO {
        private String nickname;        // 회원 닉네임
        private String profileImageUrl; // 프로필 이미지 URL
    }

    /**
     * 팔로우 상태를 포함한 회원 정보 DTO
     * 회원의 기본 정보와 함께 조회하는 사용자 기준으로 팔로우 여부를 포함
     * 외부 도메인에서 다른 사용자의 프로필을 조회할 때 팔로우 상태를 함께 표시해야 하는 경우 사용
     */
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
