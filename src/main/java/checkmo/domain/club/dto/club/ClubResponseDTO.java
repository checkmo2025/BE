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
        private String clubName;     // 모임 이름
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
        private String region;     // 모임 장소
        private String insta;        // 모임 인스타그램 URL
        private String kakao;        // 모임 카카오톡 URL
    }

}
