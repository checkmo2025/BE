package checkmo.clubMeeting.web.dto.meeting;

import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MeetingResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NextMeetingRedirect {
        private Long meetingId;
        private String redirectUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingInfo {
        private Long meetingId;
        private String title;
        private LocalDateTime meetingTime;
        private String location;
        private List<Integer> existingTeamNumbers;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Topic {
        private Long topicId;
        private String content;
        private LocalDateTime createdAt;
        private MemberExternalDTO.BasicInfo author;
        @JsonProperty("isSelected")
        private boolean selected;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamTopic {
        private Integer teamNumber;
        private List<Topic> topics;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingMemberList {
        @Schema(description = "존재하는 팀 번호 목록", example = "[1, 2, 3]")
        private List<Integer> existingTeamNumbers;
        @Schema(description = "모임 참여자 목록")
        private List<MeetingMember> members;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingMember {
        @Schema(description = "클럽 멤버십 ID", example = "101")
        private Long clubMemberId;
        @Schema(description = "참여자 정보(프로필 사진, 닉네임 정보)")
        private MemberExternalDTO.BasicInfo memberInfo; // 참여자 정보
        @JsonInclude(JsonInclude.Include.NON_NULL) // TeamMember 팀별 인원 조회에서 사용 X
        @Schema(description = "배정된 팀 번호(만약 팀이 배정되지 않았다면 null)", example = "1")
        private Integer teamNumber;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamMember {
        private Integer teamNumber; // 팀 번호
        private List<MeetingMember> members; // 해당 팀의 참여자 목록
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicSelection {
        private Long topicId;
        private Integer teamNumber; // 요청을 보낸 팀 번호
        @JsonProperty("isSelected")
        private Boolean selected; // 발제 선택 여부
    }
}
