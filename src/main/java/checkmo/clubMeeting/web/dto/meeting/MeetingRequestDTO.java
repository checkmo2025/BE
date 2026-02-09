package checkmo.clubMeeting.web.dto.meeting;

import checkmo.clubMeeting.internal.validation.validTeamManage.ValidTeamManage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MeetingRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class TopicSelection {
        @NotNull(message = "팀 번호는 필수 입력입니다.")
        @Min(value = 1, message = "팀 번호는 1 이상의 정수여야 합니다.")
        @Max(value = 12, message = "팀 번호는 12 이하의 정수여야 합니다.")
        private Integer teamNumber;

        @NotNull(message = "발제 선택 여부는 필수 입력입니다.")
        private Boolean isSelected;
    }

    @Getter
    @NoArgsConstructor
    @ValidTeamManage
    public static class TeamManage {
        @Valid
        private List<TeamMember> teamMemberList;
    }

    @Getter
    @NoArgsConstructor
    public static class TeamMember {
        @Schema(description = """
                팀 번호
                - 허용 범위: 1 ~ 12 (A조 ~ L조)
                - 중복 불가: 중복해서 팀 번호 요청 불가 예) 1조가 2개 이상 존재할 수 없음
                """, example = "1")
        @Min(value = 1, message = "팀 번호는 1 이상의 정수여야 합니다.")
        @Max(value = 12, message = "팀 번호는 12 이하의 정수여야 합니다.")
        @NotNull(message = "팀 번호는 null이 될 수 없습니다.")
        private Integer teamNumber;

        @Schema(description = """
                팀에 속할 클럽멤버 식별자 리스트
                - 조당 인원 수 범위: 1명 이상 12명 이하
                - 중복 불가: 동일 클럽멤버 식별자 중복 불가(팀 내/팀 간 중복 두 경우 다 불가)
                - 상태 조건: ACTIVE인 클럽 멤버만 조 배정 가능
                """, example = "[101, 102, 103]")
        @NotNull(message = "클럽멤버 식별자는 null이 될 수 없습니다.")
        private List<Long> clubMemberIds;
    }
}
