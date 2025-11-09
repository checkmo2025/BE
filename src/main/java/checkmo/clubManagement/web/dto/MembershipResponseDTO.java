package checkmo.clubManagement.web.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MembershipResponseDTO {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MembershipDTO {
        private Long clubMemberId; // 모임 회원 ID
        private String clubMemberStatus; // 모임 회원 상태
        private LocalDateTime updatedAt; // 모임 회원 정보 수정 시간
    }
}
