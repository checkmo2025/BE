package checkmo.member.web.dto;

import checkmo.member.internal.entity.MemberInterestCategory;
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
        private String name;
        private String description;
        private String profileImageUrl;
        private String phoneNumber;
        private Set<MemberInterestCategory> categories;
        private boolean social;
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
    public static class BlockedMember {
        private String memberId;
        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BlockedMemberList {
        private List<BlockedMember> blocks;
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
        private boolean admin;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberEmailList {
        private List<String> emails;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminMemberList {
        private List<AdminBasicInfo> memberList;
        private int page;
        private int pageSize;
        private int totalPages;
        private long totalElements;
        private boolean hasNext;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminBasicInfo {
        private String memberId;
        private String nickname;
        private String name;
        private String email;
        private String phoneNumber;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminMemberDetailInfo {
        private String memberId;
        private String nickname;
        private String name;
        private String email;
        private String phoneNumber;
        private String description;
        private String profileImageUrl;
        private Set<MemberInterestCategory> categories;
        private boolean active;
    }

}
