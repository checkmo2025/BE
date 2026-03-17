package checkmo.clubManagement.web.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ClubAdminResponseDTO {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubPreviewList {
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
}
