package checkmo.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ClubSharedDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubListDTO {
        private List<MyClubInfoDTO> clubList;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MyClubInfoDTO {
        private Long clubId;     // 모임 ID
        private String clubName; // 모임 이름
    }
}