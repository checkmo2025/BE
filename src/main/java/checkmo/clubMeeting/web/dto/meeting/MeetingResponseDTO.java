package checkmo.clubMeeting.web.dto.meeting;

import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.member.MemberExternalDTO;
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
        private boolean isSelected;
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
        private MembershipInfo membershipInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingMemberList {
        private MembershipInfo membershipInfo;
        private List<Integer> existingTeamNumbers;
        private List<MeetingMember> members; // 모임 참여자 목록
        private boolean hasNext;
        private Long nextCursor;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingMember {
        private MemberExternalDTO.BasicInfo memberInfo; // 참여자 정보
        private Integer teamNumber; // 배정된 팀 번호
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamMember {
        private Integer teamNumber; // 팀 번호
        private List<MemberExternalDTO.BasicInfo> members; // 해당 팀의 참여자 목록
        private boolean hasNext;
        private Long nextCursor;
        private MembershipInfo membershipInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicSelection {
        private Long topicId;
        private Integer teamNumber; // 요청을 보낸 팀 번호
        private Boolean isSelected; // 발제 선택 여부
    }
}
