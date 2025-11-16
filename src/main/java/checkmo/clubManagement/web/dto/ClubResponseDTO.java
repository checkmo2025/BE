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
        private List<ClubWithMyStatus> clubList; // 모임 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
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
        private List<ClubInfo> clubList; // 모임 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyPageClubList {
        private List<ClubDetail> clubList; // 모임 목록
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
        private Long clubId;         // 모임 ID
        private String clubName;     // 모임 이름, joinClub의 반환값에서 사용될 때는 null
        private Boolean open;      // 모임 공개 여부 (true: 공개, false: 비공개), MyClubListDTO-ClubInfoDTO에서 사용될 때는 null
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMemberList {
        private List<ClubMember> clubMembers; // 모임 회원 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
        private boolean isStaff; // 본인이 모임의 스탭인지 여부
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMember {
        private Long clubMemberId; // 클럽 회원 ID
        private MemberExternalDTO.BasicInfo basicInfo; // 닉네임과 프로필 url
        private String joinMessage; // 회원의 가입 메시지, ClubMemberStatus가 PENDING인 경우에만 사용됨
        private String clubMemberStatus; // 회원의 상태 (예: "MEMBER", "STAFF", "PENDING", "BLOCKED")
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRecommendList {
        private List<BookRecommendDetail> bookRecommendList; // 추천 책 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRecommendDetail {
        private Long id; // 추천 책 ID
        private String title; // 책 제목
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
        private BookExternalDTO.BasicInfo bookInfo; // 책 정보 - 공용 DTO 사용
        private MemberExternalDTO.BasicInfo authorInfo; // 추천책 작성한 회원 정보 - 공용 DTO 사용
        private boolean isAuthor; // 작성자가 본인인지 여부 (true: 본인, false: 타인)
        private boolean isStaff; // 본인이 모임의 스탭인지 여부
    }
}
