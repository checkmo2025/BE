package checkmo.domain.club.web.dto.bookshelf;

import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
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
        List<BookShelfInfoDTO> bookShelfInfoList;
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfInfoDTO {
        private MeetingInfoDTO meetingInfo;
        private BookSharedDTO.BasicInfoDTO bookInfo; // 책 정보 - 공용 DTO 사용
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingInfoDTO {
        private Long meetingId; // 모임 ID
        private int generation; // 기수
        private String tag; // 모임 태그
        private double averageRate; // Meeting의 calculateAverageRate 메소드 호출해서 값 가져오기
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfDetailDTO {
        private BookSharedDTO.DetailInfoDTO bookDetailInfo; // 책 상세 정보 - 공용 DTO 사용
        private MeetingInfoDTO meetingInfo; // Meeting 기본 정보
        private List<BookReviewDTO> bookReviewList; // 한줄평+평점 리스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookReviewListDTO {
        List<BookReviewDTO> bookReviewList; // 한줄평 리스트
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookReviewDTO {
        private Long bookReviewId; // BookReview ID
        private String description; // 한줄평 내용
        private double rate; // 평점
        private MemberSharedDTO.BasicInfoDTO authorInfo; // 작성자 정보 (globalDTO 사용)
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
        private boolean isAuthor; // 작성자가 본인인지 여부 (true: 본인, false: 타인)
    }

}
