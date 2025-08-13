package checkmo.domain.club.web.dto.meeting;

import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingResponseDTO {

    /**
     * 모임 상세 보기 페이지에서 사용할 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingDetailDTO {
        private MeetingInfoDTO meetingInfo;
        private List<TopicDTO> topics; // 모임의 토픽 목록 -> 발제 등록순 4개 담기
        private List<TeamTopicDTO> teams; // 모임의 팀 별 토픽 목록 -> 발제 등록순 4개 담기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingListDTO {
        private List<MeetingInfoDTO> meetingInfoList; // 모임 정보 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서
    }

    /**
     * 모임 목록 페이지에서 사용할 DTO -> MeetingListDTO로 커서 기반 페이지네이션
     * 캘린더 조회 페이지에서 사용할 DTO - BookSharedDTO.BasicInfoDTO, content 필드 제외
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingInfoDTO {
        private Long meetingId; // 모임 ID
        private String title; // 모임 제목
        private LocalDateTime meetingTime; // 미팅 날짜, 시간
        private String location; // 모임 장소
        private int generation; // 기수
        private String tag;
        private String content; // 모임 내용, ClubNoticeDetailDTO-MeetingNoticeDTO-MeetingInfoDTO 에서만 이 필드에 값 넣고 나머지에선 다 NULL
        private BookSharedDTO.BasicInfoDTO bookInfo; // 책 정보 - 공용 DTO 사용
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicListDTO {
        private List<TopicDTO> topics; // 토픽 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicDTO {
        private Long topicId; // 토픽 ID
        private String content; // 토픽 내용
        private MemberSharedDTO.BasicInfoDTO authorInfo; // 작성자 정보
        private List<Integer> teamNumbers; // 해당 토픽에 참여한 팀 번호 목록 | TeamTopicDTO-TopicDTO에서는 이 필드 NULL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamTopicDTO {
        private Integer teamNumber; // 팀 번호
        private List<TopicDTO> topics; // 해당 팀이 선택한 토픽 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingMemberListDTO {
        private List<MeetingMemberDTO> members; // 모임 참여자 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingMemberDTO {
        private MemberSharedDTO.BasicInfoDTO memberInfo; // 참여자 정보
        private Integer teamNumber; // 배정된 팀 번호
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamMemberDTO {
        private Integer teamNumber; // 팀 번호
        private List<MemberSharedDTO.BasicInfoDTO> members; // 해당 팀의 참여자 목록
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicSelectionDTO {
        private Long topicId; // 토픽 ID
        private Integer teamNumber; // 요청을 보낸 팀 번호
        private Boolean isSelected; // 발제 선택 여부
    }
}
