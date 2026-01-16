package checkmo.clubManagement.web.dto;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubRequestDTO {

    //TODO: 파라미터

    /**
     * 클럽 검색 필터
     *
     * @param keyword      검색 키워드
     * @param name         클럽명 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
     * @param region       지역 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
     * @param participants 대상 필터링 여부 (0: 선택 안함, 1: 선택해서 검색)
     */
    public record ClubSearchFilter(String keyword, Integer name, Integer region, Integer participants) {
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

    public record CursorInfo(Long cursorId) {
    }

    @Getter
    @NoArgsConstructor
    public static class JoinClub {
        @NotNull(message = "가입 메시지는 null일 수 없습니다.")
        @Size(max = 255, message = "가입 메시지는 255자 이하로 입력해주세요.")
        private String joinMessage;
    }

    @Getter
    @NoArgsConstructor
    public static class ClubDetail {
        @NotBlank(message = "클럽 이름은 필수 입력입니다.")
        @Size(min = 1, max = 255, message = "클럽 이름은 1자 이상 255자 이하로 입력해주세요.")
        private String name;
        @Size(max = 255, message = "클럽 설명은 255자 이하로 입력해주세요.")
        private String description;
        @Size(max = 255, message = "프로필 이미지 URL은 255자 이하로 입력해주세요.")
        private String profileImageUrl;
        private boolean open;
        @NotNull(message = "관심 카테고리는 null일 수 없습니다.")
        @Size(min = 1, max = 6, message = "관심 카테고리는 1개 이상 6개 이하로 선택해주세요.")
        private List<ClubInterestCategory> category;
        @NotBlank(message = "활동 지역은 필수 입력입니다.")
        @Size(max = 255, message = "활동 지역은 255자 이하로 입력해주세요.")
        private String region;
        private List<Club.ParticipantType> participantTypes;
        private String insta;
        private String kakao;
    }
}
