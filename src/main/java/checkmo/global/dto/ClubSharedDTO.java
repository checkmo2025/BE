package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 클럽(독서 모임) 도메인과 관련된 공유 DTO 클래스
 * 다른 도메인에서 클럽 정보를 참조할 때 사용
 */
public class ClubSharedDTO {

    /**
     * 내가 참여한 클럽 목록 응답 DTO
     * 사용자가 가입한 클럽들의 목록을 반환할 때 사용
     * 마이페이지에서 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubListDTO {
        private List<MyClubInfoDTO> clubList;
    }

    /**
     * 내 클럽 기본 정보 DTO
     * 클럽의 기본적인 정보(ID, 이름)를 포함
     * 사용자가 참여한 클럽을 간략히 표시할 때 사용
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubInfoDTO {
        private Long clubId;     // 모임 ID
        private String clubName; // 모임 이름
    }
}