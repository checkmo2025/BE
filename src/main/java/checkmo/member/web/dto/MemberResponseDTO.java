package checkmo.member.web.dto;

import checkmo.member.internal.entity.MemberInterestCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowList {
        private List<BasicInfoWithFollow> followList; // 팔로워/팔로잉 목록
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowCount {
        private long followerCount;
        private long followingCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfoWithFollow {
        private String nickname;
        private String profileImageUrl;
        private boolean isFollowing;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfoWithDescription {
        private String nickname;
        private String description;
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private String nickname;
        private String description;
        private String profileImageUrl;
        private Set<MemberInterestCategory> categories;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class othersDetailInfo {
        private String nickname;
        private String description;
        private String profileImageUrl;
        private boolean following;
        private long followerCount;
        private long followingCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FindEmailResult {
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedMember {
        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedMemberList {
        private List<RecommendedMember> friends;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportInfo {
        private Long reportId;
        private String reportedMemberNickname;
        private String reportedMemberProfileImageUrl;
        private String reportType;
        private String content;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportList {
        private List<ReportInfo> reports;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyReportInfo {
        private String reportedMemberNickname;
        private String reportedMemberProfileImageUrl;
        private String reportType;
        private String content;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime reportDate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyReportList {
        private List<MyReportInfo> reports;
        private boolean hasNext;
        private Long nextCursor;
    }
  
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginStatus {
        private String provider;
        private String email;
    }
}
