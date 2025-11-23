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
        private Set<MemberInterestCategory> categories;
    }
}
