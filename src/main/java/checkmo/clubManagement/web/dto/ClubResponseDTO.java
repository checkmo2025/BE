package checkmo.clubManagement.web.dto;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.member.MemberExternalDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubResponseDTO {

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
        private List<ClubInterestCategory> category;
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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRecommendList {
        private List<BookRecommendDetail> bookRecommendList;
        private boolean hasNext;
        private Long nextCursor;
        private int pageSize;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRecommendDetail {
        private Long id;
        private String title;
        private String content;
        private double rate;
        private String tag; // 추천 태그
        private BookExternalDTO.BasicInfo bookInfo;
        private MemberExternalDTO.BasicInfo authorInfo;
        private boolean isAuthor;
        private boolean isStaff;
    }
}
