package checkmo.clubManagement.web.dto;

import checkmo.clubManagement.internal.entity.ClubContact;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.clubManagement.internal.entity.ClubParticipantType;
import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubResponseDTO {

    public enum MyClubMemberStatus {
        NONE,
        PENDING,
        MEMBER,
        STAFF,
        OWNER,
        WITHDRAWN,
        KICKED
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubCategoryItem {
        private String code;
        private String description;

        public static ClubCategoryItem from(ClubInterestCategory clubInterestCategory) {
            return ClubCategoryItem.builder()
                    .code(clubInterestCategory.name())
                    .description(clubInterestCategory.getDescription())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubParticipantTypeItem {
        private String code;
        private String description;

        public static ClubParticipantTypeItem from(ClubParticipantType clubParticipantType) {
            return ClubParticipantTypeItem.builder()
                    .code(clubParticipantType.name())
                    .description(clubParticipantType.getDescription())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubContactItem {
        private String link;
        private String label;

        public static ClubContactItem from(ClubContact clubContact) {
            return ClubContactItem.builder()
                    .link(clubContact.getLink())
                    .label(clubContact.getLabel())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubDetail {
        private Long clubId;
        private String name;
        @JsonInclude(JsonInclude.Include.NON_NULL) // 독서모임 검색
        private String description;
        private String profileImageUrl;
        private boolean isOpen;
        private String region;
        private List<ClubCategoryItem> category;
        private List<ClubParticipantTypeItem> participantTypes;
        @JsonInclude(JsonInclude.Include.NON_NULL) // 독서모임 검색
        private List<ClubContactItem> links;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubList {
        private List<ClubDetailWithMyStatus> clubList;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubDetailWithMyStatus {
        private ClubDetail club;
        private MyClubMemberStatus myStatus;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyMembership {
        @Schema(description = "모임 ID", example = "12345")
        private Long clubId;
        @Schema(description = """
                회원의 모임 내 상태
                - NONE: 모임에서 아무런 이력이 없는 상태
                - PENDING: 모임 가입을 신청해서 승인을 대기중인 상태
                - MEMBER: 모임의 정식 회원으로 활동 중인 상태
                - STAFF: 모임의 운영진으로 활동 중인 상태
                - OWNER: 모임의 소유자(최고 운영자)인 상태
                - WITHDRAWN: 모임에서 자진 탈퇴한 상태
                - KICKED: 모임에서 강제 탈퇴된 상태
                """,
                example = "MEMBER")
        private MyClubMemberStatus myStatus;
        @JsonProperty("isActive")
        @Schema(description = "회원이 모임에서 활동 가능한지 여부(MEMBER, STAFF, OWNER이면 true, 그외 false)", example = "true")
        private boolean active;
        @JsonProperty("isStaff")
        @Schema(description = "회원이 모임의 운영진인지 여부(STAFF, OWNER이면 ture, 그외 false)", example = "false")
        private boolean staff;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMemberList {
        private List<ClubMember> clubMembers; // 모임 회원 목록
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMember {
        private Long clubMemberId;
        private MemberExternalDTO.DetailInfo detailInfo;
        @JsonInclude(JsonInclude.Include.NON_NULL) // ClubMemberStatus가 PENDING인 경우에만 사용됨
        private String joinMessage;
        private String clubMemberStatus;
        @JsonInclude(JsonInclude.Include.NON_NULL) // ClubMemberStatus가 PENDING인 경우에만 사용됨
        private LocalDateTime appliedAt;
        @JsonInclude(JsonInclude.Include.NON_NULL) // ClubMemberStatus가 MEMBER, STAFF, OWNER인 경우에만 사용됨
        private LocalDateTime joinedAt;
    }
}
