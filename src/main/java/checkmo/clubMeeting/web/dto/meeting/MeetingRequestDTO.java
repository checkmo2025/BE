package checkmo.clubMeeting.web.dto.meeting;

import checkmo.clubMeeting.internal.validation.validTeamManage.ValidTeamManage;
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
        private Integer teamNumber;

        @NotNull(message = "발제 선택 여부는 필수 입력입니다.")
        private Boolean isSelected;
    }

    @Getter
    @NoArgsConstructor
    @ValidTeamManage
    public static class TeamManage {
        @Valid
        @NotNull(message = "팀 멤버 정보는 null이 될 수 없습니다.")
        private List<TeamMember> teamMemberList;
    }

    @Getter
    @NoArgsConstructor
    public static class TeamMember {
        @Min(value = 1, message = "팀 번호는 1 이상의 정수여야 합니다.")
        @Max(value = 12, message = "팀 번호는 12 이하의 정수여야 합니다.")
        @NotNull(message = "팀 번호는 null이 될 수 없습니다.")
        private Integer teamNumber;

        @NotNull(message = "클럽멤버 식별자는 null이 될 수 없습니다.")
        private List<Long> clubMemberIds;
    }
}
