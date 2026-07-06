package checkmo.clubManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 클럽 운영 모듈과 관련된 다른 모듈에게 public한 DTO 클래스
 */
public class ClubManagementExternalDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubList {
        private List<BasicInfo> clubList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BasicInfo {
        private Long clubId;
        private String clubName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DisplayInfo {
        private Long clubId;
        private String clubName;
        private String clubImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MembershipInfo {
        private Long memberId;
        private Long clubMemberId;
        private boolean active;
        private boolean staff;
    }
}
