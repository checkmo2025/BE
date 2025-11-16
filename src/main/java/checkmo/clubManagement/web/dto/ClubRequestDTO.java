package checkmo.clubManagement.web.dto;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubRequestDTO {

    /**
     * 클럽 검색 필터
     *
     * @param keyword      검색 키워드
     * @param name         클럽명 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
     * @param region       지역 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
     * @param participants 대상 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
     */
    public record ClubSearchFilter(
            String keyword,
            Integer name,
            Integer region,
            Integer participants
    ) {
        public ClubSearchFilter {
            if (keyword == null) {
                keyword = "";
            }
            if (name == null) {
                name = 0;
            }
            if (region == null) {
                region = 0;
            }
            if (participants == null) {
                participants = 0;
            }
        }
    }

    public record CursorInfo(
            Long cursorId,
            Integer size
    ) {}

    @Getter
    @NoArgsConstructor
    public static class JoinClub {
        private String joinMessage;
    }

    @Getter
    @NoArgsConstructor
    public static class ClubDetail {
        @NotBlank
        private String name;
        private String description;
        private String profileImageUrl;
        private boolean open;
        private List<ClubInterestCategory> category;
        private String region;
        private List<Club.ParticipantType> participantTypes;
        private String insta;
        private String kakao;
    }

    @Getter
    @NoArgsConstructor
    public static class CreateBookRecommend {
        private String title;
        private BookExternalDTO.BookCreate bookDetail; // 책 정보
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateBookRecommend {
        private String title; // 추천 제목
        private String content; // 추천 내용
        private double rate; // 평점
        private String tag; // 추천 태그
    }
}
