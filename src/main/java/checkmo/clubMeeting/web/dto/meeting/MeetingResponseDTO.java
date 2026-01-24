package checkmo.clubMeeting.web.dto.meeting;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    public static class MeetingDetail {
        private MembershipInfo membershipInfo;
        private MeetingInfo meetingInfo;
        private List<Topic> topics; // 모임의 토픽 목록 -> 발제 등록순 4개 담기
        private List<TeamTopic> teams; // 모임의 팀 별 토픽 목록 -> 발제 등록순 4개 담기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingList {
        private MembershipInfo membershipInfo;
        private List<MeetingInfo> meetingInfoList;
        private boolean hasNext;
        private Long nextCursor;
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
        private Integer generation;
        private String tag;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private BookExternalDTO.BasicInfo bookInfo; // CalendarMeeting - List<MeetingInfo> 캘린더 조회할 때 NULL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarMeeting {
        private List<MeetingInfo> meetingInfoList;
        private MembershipInfo membershipInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Topic {
        private Long topicId;
        private String content;
        private MemberExternalDTO.BasicInfo authorInfo;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<Integer> teamNumbers; // 해당 토픽에 참여한 팀 번호 목록 | TeamTopic-topics 이 필드 NULL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicDTO {
        private List<Topic> topics;
        private MembershipInfo membershipInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamTopic {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private MembershipInfo membershipInfo;
        private Integer teamNumber; // 팀 번호
        private List<Topic> topics; // 해당 팀이 선택한 토픽 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingMemberList {
        private MembershipInfo membershipInfo;
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
        private MembershipInfo membershipInfo;
        private Integer teamNumber; // 팀 번호
        private List<MemberExternalDTO.BasicInfo> members; // 해당 팀의 참여자 목록
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
