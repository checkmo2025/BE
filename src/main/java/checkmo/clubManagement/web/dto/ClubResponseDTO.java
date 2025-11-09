package checkmo.clubManagement.web.dto;

import checkmo.book.BookSharedDTO;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.member.MemberSharedDTO;
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
    public static class ClubListDTO {
        private List<ClubWithMyStatusDTO> clubList; // 모임 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubWithMyStatusDTO {
        private ClubResponseDTO.ClubDetailDTO club;
        private boolean isMember;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubListDTO {
        private List<ClubInfoDTO> clubList; // 모임 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyPageClubListDTO {
        private List<ClubDetailResponseDTO> clubList; // 모임 목록
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubDetailResponseDTO {
        private Long clubId;
        private String name;
        private String description;
        private String profileImageUrl;
        private boolean open;
        private List<String> category; // String으로 주기 -> 마이페이지용
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
    public static class ClubInfoDTO {
        private Long clubId;         // 모임 ID
        private String clubName;     // 모임 이름, joinClub의 반환값에서 사용될 때는 null
        private Boolean open;      // 모임 공개 여부 (true: 공개, false: 비공개), MyClubListDTO-ClubInfoDTO에서 사용될 때는 null
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubDetailDTO {
        private Long clubId;         // 모임 ID
        private String name;
        private String description;
        private String profileImageUrl;
        private boolean open;
        private List<Long> category;
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
    public static class ClubMemberListDTO {
        private List<ClubMemberDTO> clubMembers; // 모임 회원 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
        private boolean isStaff; // 본인이 모임의 스탭인지 여부
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMemberUpdateResponseDTO {
        private ClubResponseDTO.ClubMemberDTO updatedMember;
        private boolean isRequesterStaff; // 현재 로그인한 요청자가 운영진인지 여부
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMemberDTO {
        private Long clubMemberId; // 클럽 회원 ID
        private MemberSharedDTO.BasicInfo basicInfo; // 닉네임과 프로필 url
        private String joinMessage; // 회원의 가입 메시지, ClubMemberStatus가 PENDING인 경우에만 사용됨
        private String clubMemberStatus; // 회원의 상태 (예: "MEMBER", "STAFF", "PENDING", "BLOCKED")
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRecommendListDTO {
        private List<BookRecommendDetailDTO> bookRecommendList; // 추천 책 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookRecommendDetailDTO {
        private Long id; // 추천 책 ID
        private String title; // 책 제목
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
        private BookSharedDTO.BasicInfo bookInfo; // 책 정보 - 공용 DTO 사용
        private MemberSharedDTO.BasicInfo authorInfo; // 추천책 작성한 회원 정보 - 공용 DTO 사용
        private boolean isAuthor; // 작성자가 본인인지 여부 (true: 본인, false: 타인)
        private boolean isStaff; // 본인이 모임의 스탭인지 여부
    }
}
