package checkmo.clubManagement;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 클럽 운영 모듈과 관련된 다른 모듈에게 public한 DTO 클래스
 */
public class ClubManagementExternalDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubList {
        private List<MyClubInfo> clubList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubInfo {
        private Long clubId;
        private String clubName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Membership {
        private String memberId;
        private Long clubMemberId;
        private boolean active;
        private boolean staff;
    }
}
