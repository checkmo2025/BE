package checkmo.clubMeeting;

import checkmo.book.BookExternalDTO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 클럽 모임 모듈과 관련된 다른 모듈에게 public한 DTO 클래스
 */
public class ClubMeetingExternalDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingInfo {
        private Long meetingId; // 모임 ID
        private String title; // 모임 제목
        private LocalDateTime meetingTime; // 미팅 날짜, 시간
        private String location; // 모임 장소
        private int generation; // 기수
        private String tag; // 사용자 작성 태그
        private String content; // 모임 내용
        private BookExternalDTO.BasicInfo bookInfo; // 책 정보 - 공용 DTO 사용
    }

}
