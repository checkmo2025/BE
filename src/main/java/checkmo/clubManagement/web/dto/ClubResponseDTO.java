package checkmo.clubManagement.web.dto;

import checkmo.clubManagement.internal.entity.ClubContact;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.clubManagement.internal.entity.ClubParticipantType;
import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
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
        BLOCKED
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
        private Long clubId;
        private MyClubMemberStatus myStatus;
        private boolean isActive;
        private boolean isStaff;
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
