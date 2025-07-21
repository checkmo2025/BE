package checkmo.domain.club.web.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ClubRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class ClubMemberJoinDTO {
        private String joinMessage;
    }

    @Getter
    @NoArgsConstructor
    public static class ClubDetailDTO {
        private String name;
        private String description;
        private String profileImageUrl;
        private boolean isOpen;
        private List<Long> category;
        private String region;
        private String purpose;
        private String participants;
        private String insta;
        private String kakao;
    }

    @Getter
    @NoArgsConstructor
    public static class CreateClubNoticeDTO {
        private String title;
        private String content;
        private boolean important;
    }

    @Getter
    @NoArgsConstructor
    public static class CreateClubVoteDTO {
        private String title;
        private boolean important;

        @NotNull
        private String item1;

        @NotNull
        private String item2;

        private String item3;
        private String item4;
        private String item5;
        private boolean isAnonymity;
        private boolean isDuplication;
        private LocalDateTime startTime;
        private LocalDateTime deadline;
    }

    @Getter
    @NoArgsConstructor
    public static class VoteResultDTO {
        private boolean item1;
        private boolean item2;
        private boolean item3;
        private boolean item4;
        private boolean item5;
    }

    @Getter
    @NoArgsConstructor
    public static class CreateBookRecommendDTO {
        private String title;
        private BookDetailDTO bookDetail; // 책 정보
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
    }

    @Getter
    @NoArgsConstructor
    public static class BookDetailDTO {
        @NotBlank(message = "ISBN 번호는 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String isbn; // ISBN 번호
        @NotBlank(message = "책 제목은 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String title; // 책 제목
        @NotBlank(message = "저자는 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String author; // 저자
        @NotBlank(message = "책 이미지 URL은 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String imgUrl; // 책 이미지 URL
        @NotBlank(message = "출판사는 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String publisher; // 출판사
        @NotBlank(message = "책 설명은 null, 빈 문자열(\"\"), 공백 문자(\" \")까지도 모두 허용하지 않습니다.")
        private String description; // 책 설명
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateBookRecommendDTO {
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
    }
}
