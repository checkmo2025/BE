package checkmo.domain.club.dto.club;

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
    public static class ClubListDTO {
        private List<ClubDetailDTO> clubList; // 모임 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
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
    public static class ClubInfoDTO {
        private Long clubId;         // 모임 ID
        private String clubName;     // 모임 이름, joinClub의 반환값에서 사용될 때는 null
        private Boolean isOpen;      // 모임 공개 여부 (true: 공개, false: 비공개), MyClubListDTO-ClubInfoDTO에서 사용될 때는 null
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubDetailDTO {
        private Long clubId;         // 모임 ID
        private String profileImgUrl;// 모임 프로필 이미지 URL
        private String clubName;     // 모임 이름
        private String description;  // 모임 소개
        private String participants; // 모임 대상
        private String category;     // 모임 카테고리
        private String region;       // 모임 장소
        private String insta;        // 모임 인스타그램 URL
        private String kakao;        // 모임 카카오톡 URL
        private boolean isOpen;      // 모임 공개 여부 (true: 공개, false: 비공개)
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
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClubMemberDTO {
        private String nickname; // 회원의 닉네임
        private String profileImgUrl; // 회원의 프로필 이미지 URL
        private String joinMessage; // 회원의 가입 메시지, ClubMemberStatus가 PENDING인 경우에만 사용됨
        private String clubMemberStatus; // 회원의 상태 (예: "MEMBER", "STAFF", "PENDING", "BLOCKED")
    }
}
