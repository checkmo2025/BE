package checkmo.club.web.dto.meeting;

import checkmo.club.validation.validTeamManage.ValidTeamManage;
import checkmo.book.BookSharedDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class MeetingCreateRequestDTO {
        @NotBlank(message = "독서모임 제목은 필수 입력입니다.")
        @Size(max = 255, message = "독서모임 제목은 255자 이하로 입력해주세요.")
        private String title; // 독서모임 제목
        @NotNull(message = "미팅 날짜, 시간은 null이 될 수 없습니다.")
        private LocalDateTime meetingTime; // 미팅 날짜, 시간
        @NotBlank(message = "독서모임 장소는 필수 입력입니다.")
        @Size(max = 255, message = "독서모임 장소는 255자 이하로 입력해주세요.")
        private String location; // 모임 장소
        @NotBlank(message = "독서모임 설명은 필수 입력입니다.")
        @Size(max = 1000, message = "독서모임 설명은 1000자 이하로 입력해주세요.")
        private String content;  // 모임 내용 설명 (공지사항 내용)
        @NotNull(message = "기수는 null이 될 수 없습니다.")
        @Min(value = 1, message = "기수는 1 이상의 정수여야 합니다.")
        private Integer generation;  // 기수
        @NotBlank(message = "독서모임 태그는 필수 입력입니다.")
        @Size(max = 6, message = "태그는 최대 6글자까지 입력 가능합니다.")
        private String tag; // 태그
        @Valid
        @NotNull(message = "책 정보는 null이 될 수 없습니다.")
        private BookSharedDTO.BookCreateRequest bookInfo;
    }

    @Getter
    @NoArgsConstructor
    public static class MeetingUpdateRequestDTO {
        @NotBlank(message = "독서모임 제목은 필수 입력입니다.")
        @Size(max = 255, message = "독서모임 제목은 255자 이하로 입력해주세요.")
        private String title; // 독서모임 제목
        @NotNull(message = "미팅 날짜, 시간은 null이 될 수 없습니다.")
        private LocalDateTime meetingTime; // 미팅 날짜, 시간
        @NotBlank(message = "독서모임 장소는 필수 입력입니다.")
        @Size(max = 255, message = "독서모임 장소는 255자 이하로 입력해주세요.")
        private String location; // 모임 장소
        @NotBlank(message = "독서모임 설명은 필수 입력입니다.")
        @Size(max = 1000, message = "독서모임 설명은 1000자 이하로 입력해주세요.")
        private String content;  // 모임 내용 설명 (공지사항 내용)
        @NotNull(message = "기수는 null이 될 수 없습니다.")
        @Min(value = 1, message = "기수는 1 이상의 정수여야 합니다.")
        private Integer generation;  // 기수
        @NotBlank(message = "독서모임 태그는 필수 입력입니다.")
        @Size(max = 6, message = "태그는 최대 6글자까지 입력 가능합니다.")
        private String tag; // 태그
    }

    @Getter
    @NoArgsConstructor
    public static class TopicSelectionDTO {
        @NotNull(message = "팀 번호는 필수 입력입니다.")
        @Min(value = 1, message = "팀 번호는 1 이상의 정수여야 합니다.")
        private Integer teamNumber; // 팀 번호
        @NotNull(message = "발제 선택 여부는 필수 입력입니다.")
        private Boolean isSelected; // 발제 선택 여부
    }

    @Getter
    @NoArgsConstructor
    @ValidTeamManage
    public static class TeamManageDTO {
        @Valid
        @NotNull(message = "팀 멤버 정보는 null이 될 수 없습니다.")
        private List<TeamMemberDTO> teamMemberDTOList;
    }

    @Getter
    @NoArgsConstructor
    public static class TeamMemberDTO {
        @Min(value = 1, message = "팀 번호는 1 이상의 정수여야 합니다.")
        @NotNull(message = "팀 번호는 null이 될 수 없습니다.")
        private Integer teamNumber; // 팀 번호
        @NotNull(message = "닉네임 리스트는 null이 될 수 없습니다.")
        private List<
                @NotBlank(message = "닉네임은 필수입니다")
                @Pattern(regexp = "^[a-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$",
                        message = "닉네임은 영어 소문자 및 특수문자만 사용 가능합니다")
                        // @Size(max = 6, message = "닉네임은 6자 이하여야 합니다.")
                        String> nicknameList; // 팀원들의 닉네임 리스트, 닉네임에 대한 검증은 AdditionalInfoDTO에서 가져왔습니다
    }
}
