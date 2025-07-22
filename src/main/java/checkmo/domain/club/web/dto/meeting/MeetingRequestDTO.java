package checkmo.domain.club.web.dto.meeting;

import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class MeetingCreateRequestDTO {
        @NotBlank(message = "독서모임 제목은 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String title; // 독서모임 제목
        @NotNull(message = "미팅 날짜, 시간은 null이 될 수 없습니다.")
        private LocalDateTime meetingTime; // 미팅 날짜, 시간
        @NotBlank(message = "독서모임 장소는 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String location; // 모임 장소
        @NotBlank(message = "독서모임 설명은 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String content;  // 모임 내용 설명 (공지사항 내용)
        @Min(value = 1, message = "기수는 1 이상의 정수여야 합니다.")
        private int generation;  // 기수
        @NotBlank(message = "독서모임 태그는 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String tag; // 태그
        @Valid
        private ClubRequestDTO.BookDetailDTO bookDetail; // 책 정보
    }

    @Getter
    @NoArgsConstructor
    public static class MeetingUpdateRequestDTO {
        @NotBlank(message = "독서모임 제목은 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String title; // 독서모임 제목
        @NotNull(message = "미팅 날짜, 시간은 null이 될 수 없습니다.")
        private LocalDateTime meetingTime; // 미팅 날짜, 시간
        @NotBlank(message = "독서모임 장소는 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String location; // 모임 장소
        @NotBlank(message = "독서모임 설명은 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String content;  // 모임 내용 설명 (공지사항 내용)
        @Min(value = 1, message = "기수는 1 이상의 정수여야 합니다.")
        private int generation;  // 기수
        @NotBlank(message = "독서모임 태그는 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String tag; // 태그
    }

    @Getter
    @NoArgsConstructor
    public static class TopicDTO {
        private String description; // 토픽 내용
    }

    @Getter
    @NoArgsConstructor
    public static class TeamManageDTO {
        private List<TeamMemberDTO> teamMemberDTOList;
    }

    @Getter
    @NoArgsConstructor
    public static class TeamMemberDTO {
        private Integer teamNumber; // 팀 번호
        private List<String> nicknameList; // 팀원들의 닉네임 리스트
    }

    @Getter
    @NoArgsConstructor
    public static class TopicManageDTO {
        private Long topicId; // 발제 ID
        private Integer teamNumber; // 팀 번호
    }
}
