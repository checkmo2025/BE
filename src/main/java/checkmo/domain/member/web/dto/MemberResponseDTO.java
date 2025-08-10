package checkmo.domain.member.web.dto;

import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.CategorySharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponseDTO {

    /**
     * 팔로잉 목록 조회 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowList {
        private List<MemberSharedDTO.WithFollowStatusDTO> followList; // 팔로워/팔로잉 목록
        private boolean hasNext;        // 다음 페이지 존재 여부
        private Long nextCursor;        // 다음 페이지 커서 (마지막 항목의 ID)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowPreviewList {
        private List<MemberSharedDTO.WithFollowStatusDTO> followList; // 팔로워/팔로잉 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberProfileResponseDTO {
        private String nickname;
        private String description;
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberProfileWithCategoryResponseDTO {
        private String nickname;
        private String description;
        private String profileImageUrl;
        private List<CategorySharedDTO.CategoryInfo> categories; // 카테고리 정보 리스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpResponseDTO {
        private String email;
        private boolean isProfileCompleted;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class otherProfileResponseDTO {
        private String nickname;
        private String description;
        private String profileImageUrl;
        private boolean isFollowed;
        private List<BookStoryPreviewDTO> bookStories;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookStoryPreviewDTO {
        private Long bookStoryId;
        private String bookStoryTitle;
        private BookSharedDTO.BasicInfoDTO bookInfo; // 책 정보 - 공용 DTO 사용
        private int likes;
        private LocalDateTime createdAt;
        private boolean isLiked; // 조회하는 사람의 좋아요 여부
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponseDTO {
        private String nickname;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PresignedUrlDTO {
        private String presignedUrl; //프론트가 사용할 URL
        private String imageUrl; //우리 백엔드에 저장할 URL
    }
}
