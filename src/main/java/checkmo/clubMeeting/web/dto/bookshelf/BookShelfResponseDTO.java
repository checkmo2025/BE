package checkmo.clubMeeting.web.dto.bookshelf;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.member.MemberExternalDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BookShelfResponseDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfListDTO {
        private List<BookShelfInfoDTO> bookShelfInfoList;
        private ClubManagementExternalDTO.MembershipDTO membership;
        private boolean hasNext; // 다음 페이지 존재 여부
        private Long nextCursor; // 다음 페이지 커서 (마지막 항목의 ID)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookShelfInfoDTO {
        private MeetingInfoDTO meetingInfo;
        private BookExternalDTO.BasicInfo bookInfo; // 책 정보 - 공용 DTO 사용
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
        private ClubManagementExternalDTO.MembershipDTO membership;
        private MeetingInfoDTO meetingInfo; // Meeting 기본 정보
        private BookExternalDTO.DetailInfo bookDetailInfo; // 책 상세 정보 - 공용 DTO 사용
        private TopicListDTO topicList; // 발제 리스트(등록순 3개 미리보기)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookReviewListDTO {
        private List<BookReviewDTO> bookReviewList; // 한줄평 리스트
        private ClubManagementExternalDTO.MembershipDTO membership;
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
        private MemberExternalDTO.BasicInfo authorInfo; // 작성자 정보 (globalDTO 사용)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopicListDTO {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private ClubManagementExternalDTO.MembershipDTO membership;
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
        private MemberExternalDTO.BasicInfo authorInfo; // 작성자 정보
        private boolean isAuthor; // 작성자가 본인인지 여부 (true: 본인, false: 타인)
    }

}
