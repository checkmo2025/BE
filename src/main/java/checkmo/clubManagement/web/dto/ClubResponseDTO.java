package checkmo.clubManagement.web.dto;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.member.MemberExternalDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ClubResponseDTO {

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
    public static class ClubList {
        private List<ClubWithMyStatus> clubList;
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubWithMyStatus {
        private ClubDetail club;
        private boolean isMember;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubList {
        private List<ClubInfo> clubList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyPageClubList {
        private List<ClubDetail> clubList;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubDetail {
        private Long clubId;
        private String name;
        private String description;
        private String profileImageUrl;
        private boolean open;
        private List<ClubCategoryItem> category;
        private String region;
        private List<Club.ParticipantType> participantTypes;
        private String insta;
        private String kakao;
        private boolean isStaff;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubInfo {
        private Long clubId;
        private String clubName;
        private Boolean open;      // 모임 공개 여부 (true: 공개, false: 비공개), MyClubList-ClubInfoDTO에서 사용될 때는 null
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMemberList {
        private List<ClubMember> clubMembers; // 모임 회원 목록
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
        private boolean isStaff;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMember {
        private Long clubMemberId;
        private MemberExternalDTO.BasicInfo basicInfo;
        private String joinMessage; // 회원의 가입 메시지, ClubMemberStatus가 PENDING인 경우에만 사용됨
        private String clubMemberStatus;
    }
}
