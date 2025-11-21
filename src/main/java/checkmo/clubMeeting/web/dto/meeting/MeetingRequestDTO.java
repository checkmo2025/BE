package checkmo.clubMeeting.web.dto.meeting;

import checkmo.book.BookExternalDTO;
import checkmo.clubMeeting.internal.validation.validTeamManage.ValidTeamManage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MeetingRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class MeetingCreate {
        @NotBlank(message = "독서모임 제목은 필수 입력입니다.")
        @Size(max = 255, message = "독서모임 제목은 255자 이하로 입력해주세요.")
        private String title;

        @NotNull(message = "미팅 날짜, 시간은 null이 될 수 없습니다.")
        private LocalDateTime meetingTime;

        @NotBlank(message = "독서모임 장소는 필수 입력입니다.")
        @Size(max = 255, message = "독서모임 장소는 255자 이하로 입력해주세요.")
        private String location;

        @NotBlank(message = "독서모임 설명은 필수 입력입니다.")
        @Size(max = 1000, message = "독서모임 설명은 1000자 이하로 입력해주세요.")
        private String content;

        @NotNull(message = "기수는 null이 될 수 없습니다.")
        @Min(value = 1, message = "기수는 1 이상의 정수여야 합니다.")
        private Integer generation;

        @NotBlank(message = "독서모임 태그는 필수 입력입니다.")
        @Size(max = 6, message = "태그는 최대 6글자까지 입력 가능합니다.")
        private String tag;

        @Valid
        @NotNull(message = "책 정보는 null이 될 수 없습니다.")
        private BookExternalDTO.BookCreate bookInfo;
    }

    @Getter
    @NoArgsConstructor
    public static class MeetingUpdate {
        @NotBlank(message = "독서모임 제목은 필수 입력입니다.")
        @Size(max = 255, message = "독서모임 제목은 255자 이하로 입력해주세요.")
        private String title;

        @NotNull(message = "미팅 날짜, 시간은 null이 될 수 없습니다.")
        private LocalDateTime meetingTime;

        @NotBlank(message = "독서모임 장소는 필수 입력입니다.")
        @Size(max = 255, message = "독서모임 장소는 255자 이하로 입력해주세요.")
        private String location;

        @NotBlank(message = "독서모임 설명은 필수 입력입니다.")
        @Size(max = 1000, message = "독서모임 설명은 1000자 이하로 입력해주세요.")
        private String content;

        @NotNull(message = "기수는 null이 될 수 없습니다.")
        @Min(value = 1, message = "기수는 1 이상의 정수여야 합니다.")
        private Integer generation;

        @NotBlank(message = "독서모임 태그는 필수 입력입니다.")
        @Size(max = 6, message = "태그는 최대 6글자까지 입력 가능합니다.")
        private String tag;
    }

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
        @NotNull(message = "팀 번호는 null이 될 수 없습니다.")
        private Integer teamNumber;

        @NotNull(message = "클럽멤버 식별자는 null이 될 수 없습니다.")
        private List<Long> clubMemberIds;
    }
}
