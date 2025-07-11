package checkmo.domain.club.dto.bookshelf;

import checkmo.domain.club.dto.meeting.MeetingResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class BookShelfResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfListDTO {
        List<BookShelfInfoDTO> bookShelfInfoDTOList;
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
        private int pageSize; // 현재 페이지 크기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfInfoDTO {
        private MeetingResponseDTO.BookInfoDTO bookInfoDTO;
        private MeetingInfoDTO meetingInfoDTO;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingInfoDTO {
        private Long meetingId; // 모임 ID
        private int generation; // 기수
        private String tag;
        private double averageRate; // Meeting의 calculateAverageRate 메소드 호출해서 값 가져오기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfDetailDTO {
        private MeetingResponseDTO.BookDetailInfoDTO bookDetailInfoDTO; // 책 정보 재사용
        private MeetingInfoDTO meetingInfoDTO; // Meeting 기본 정보
        private List<BookReviewDTO> bookReviewList; // 한줄평+평점 리스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookReviewDTO {
        private Long bookReviewId; // BookReview ID
        private String description; // 한줄평 내용
        private double rate; // 평점
        private MeetingResponseDTO.MemberDTO authorInfo; // 작성자 정보 (기존 DTO 재사용)
    }

}
