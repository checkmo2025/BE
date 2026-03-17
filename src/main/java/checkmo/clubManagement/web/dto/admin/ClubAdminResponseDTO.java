package checkmo.clubManagement.web.dto.admin;

import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ClubAdminResponseDTO {
    public enum ActiveClubMemberStatus {
        OWNER,
        STAFF,
        MEMBER;

        public static ActiveClubMemberStatus of(ClubMemberStatus status) {
            return switch (status) {
                case OWNER -> ActiveClubMemberStatus.OWNER;
                case STAFF -> ActiveClubMemberStatus.STAFF;
                case MEMBER -> ActiveClubMemberStatus.MEMBER;
                default -> throw new ClubManagementException(ClubManagementErrorStatus.CLUB_MEMBER_INVALID_STATUS);
            };
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubPreviewPage {
        private List<ClubPreview> clubs;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubPreview {
        private Long clubId;
        private String clubName;
        private String ownerEmail;
        private LocalDateTime createdAt;
        private long memberCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubActiveMemberPreviewPage {
        private List<ClubActiveMemberPreview> members;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubActiveMemberPreview {
        private String nickname;
        private String name;
        private String email;
        private String phoneNumber;
        private LocalDateTime joinedAt;
        private ActiveClubMemberStatus role;
    }
}
