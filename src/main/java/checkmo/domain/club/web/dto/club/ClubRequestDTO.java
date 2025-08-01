package checkmo.domain.club.web.dto.club;

import checkmo.domain.category.web.dto.CategoryRequestDTO;
import checkmo.domain.club.entity.Club;
import checkmo.global.dto.BookSharedDTO;
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
        private boolean open;
        private List<Long> category;
        private String region;
        private List<Club.ParticipantType> participantTypes;
        private String insta;
        private String kakao;

        public CategoryRequestDTO.CategoryListRequestDTO toCategoryListRequestDTO() {
            return CategoryRequestDTO.CategoryListRequestDTO.builder()
                    .categoryIdList(this.category)
                    .build();
        }
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
