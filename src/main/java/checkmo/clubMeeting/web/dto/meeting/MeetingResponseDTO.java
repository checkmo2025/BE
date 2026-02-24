package checkmo.clubMeeting.web.dto.meeting;

import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
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
    public static class TeamKey {
        @Schema(description = "팀 ID", example = "153")
        private Long teamId;
        @Schema(description = "팀 번호", example = "1")
        private Integer teamNumber;
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
        private List<TeamKey> existingTeams;
        private List<TeamMember> teamMembers;
        @Getter(AccessLevel.NONE)
        private boolean staff;

        @JsonProperty("isStaff")
        public boolean isStaff() {
            return staff;
        }
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
        @Getter(AccessLevel.NONE)
        private boolean selected;

        @JsonProperty("isSelected")
        public boolean isSelected() {
            return selected;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamTopic {
        private List<Integer> existingTeamNumbers;
        private Integer requestedTeamNumber;
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
        @Schema(description = "배정된 팀 번호(만약 팀이 배정되지 않았다면 null)", example = "1")
        private Integer teamNumber;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamMember {
        private TeamKey teamKey;
        private List<MeetingMember> members;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicSelection {
        private Long topicId;
        private Integer teamNumber; // 요청을 보낸 팀 번호
        @Getter(AccessLevel.NONE)
        private boolean selected; // 발제 선택 여부

        @JsonProperty("isSelected")
        public boolean isSelected() {
            return selected;
        }
    }
}
