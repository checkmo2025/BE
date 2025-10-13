package checkmo.domain.club.web.dto.club;

import checkmo.domain.club.entity.Club;
import checkmo.global.dto.BookSharedDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
        private boolean open;
        private List<Long> category;
        private String region;
        private List<Club.ParticipantType> participantTypes;
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
        @NotBlank                 // title은 필수
        private String title;

        @Size(max = 255)
        private String content;

        private boolean important;

        @NotNull
        private String item1;

        @NotNull
        private String item2;

        private String item3;
        private String item4;
        private String item5;
        private boolean anonymity;
        private boolean duplication;
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

        // 몇 개를 선택했는지 확인하는 DTO용 메서드로, 복수 선택 검증에서 사용됨
        public int countSelectedItems() {
            int count = 0;
            if (item1) count++;
            if (item2) count++;
            if (item3) count++;
            if (item4) count++;
            if (item5) count++;
            return count;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CreateBookRecommendDTO {
        private String title;
        private BookSharedDTO.BookCreateRequestDTO bookDetail; // 책 정보
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateBookRecommendDTO {
        private String title; // 추천 제목
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
    }
}
