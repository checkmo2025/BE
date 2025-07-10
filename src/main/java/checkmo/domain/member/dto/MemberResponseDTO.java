package checkmo.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class MemberResponseDTO {

    /**
     * 팔로잉 목록 조회 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowingListResponseDTO {
        private List<FollowResponseDTO> followingList; // 팔로잉 목록
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
    }

    /**
     * 팔로우 목록 조회 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowerListResponseDTO {
        private List<FollowResponseDTO> followerList; // 팔로우 목록
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowResponseDTO {
        private Long followId;   // 팔로우 ID (Follow 엔티티의 ID)
        private String nickname; // 회원 닉네임
        private String profileImageUrl; // 프로필 이미지 URL
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberProfileResponseDTO {
        private String nickName;
        private String description;
        private String imgUrl;
    }
}
