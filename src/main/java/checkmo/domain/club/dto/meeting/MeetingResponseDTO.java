package checkmo.domain.club.dto.meeting;

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
    public static class InProgressMeetingDetailDTO {
        private MeetingInfoDTO meetingInfoDTO;
        private List<TopicDTO> topics; // 모임의 토픽 목록 -> 최신순 최대 4개 담기
        private List<TeamDTO> teams; // 모임의 팀 별 토픽 목록 -> 최신순 최대 4개 담기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompletedMeetingDetailDTO {
        private MeetingInfoDTO meetingInfoDTO;
        private List<TopicDTO> topics; // 모임의 토픽 목록
        private List<MemberDTO> participantInfoList; // 모임 참여자 목록
        private List<TeamDTO> teams; // 모임의 팀 목록
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingListDTO {
        private List<MeetingInfoDTO> meetingInfoList; // 모임 정보 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서
        private int pageSize; // 현재 페이지 크기
    }

    /**
     * 모임 목록 페이지에서 사용할 리스트 DTO중 1개
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
        private BookInfoDTO bookInfoDTO;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicListDTO {
        private List<TopicDTO> topics; // 토픽 목록
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서
        private int pageSize; // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicDTO {
        private Long topicId; // 토픽 ID
        private String content; // 토픽 내용
        private MemberDTO authorInfo; // 작성자 정보
        private List<Integer> teamNumbers; // 해당 토픽에 참여한 팀 번호 목록 | TopicListDTO-TopicDTO,TeamDTO-TopicDTO에서는 이 필드 NULL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookInfoDTO {
        private String title; // 책 제목
        private String author; // 저자
        private String coverImageUrl; // 책 표지 이미지 URL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookDetailInfoDTO {
        private String title; // 책 제목
        private String author; // 저자
        private String coverImageUrl; // 책 표지 이미지 URL
        private String publisher; // 출판사
        private String description; // 책 설명
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberDTO {
        private Integer teamNumber; //TopicDTO-ParticipantDTO에서는 이 필드 NULL, BookShelfDetailDTO-BookReviewDTO-ParticipantDTO에서는 이 필드 NULL
        private String nickname; // 참여자 닉네임
        private String profileImageUrl; // 참여자 프로필 이미지 URL
        // TODO: 만약 운영진만 보여주고 싶다면, 운영진 여부를 나타내는 필드 추가
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TeamDTO {
        private Integer teamNumber; // 팀 번호
        private List<TopicDTO> topics; // 해당 팀이 선택한 토픽 목록
        private List<MemberDTO> participantInfoList; // 팀원 목록, InProgressMeetingDetailDTO-TeamDTO에서는 이 필드 NULL
    }
}
