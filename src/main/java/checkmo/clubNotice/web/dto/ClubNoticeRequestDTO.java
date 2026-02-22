package checkmo.clubNotice.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubNoticeRequestDTO {
    @Getter
    @NoArgsConstructor
    public static class CreateClubNotice {
        @NotBlank(message = "공지사항 제목은 필수 입력입니다.")
        @Size(max = 40, message = "공지사항 제목은 40자 이하로 입력해주세요.")
        private String title;
        @NotNull(message = "공지사항 내용은 null이 될 수 없습니다.")
        @Size(max = 1000, message = "공지사항 내용은 1000자 이하로 입력해주세요.")
        private String content;
        @Getter(AccessLevel.NONE)
        private boolean pinned;
        private Long meetingId;
        @Size(max = 5, message = "공지사항 이미지는 최대 5개까지 가능합니다.")
        private List<@NotBlank(message = "공지사항 이미지 URL은 비어있을 수 없습니다.") String> imageUrls;
        @Valid
        private CreateClubVote vote;

        @JsonProperty("isPinned")
        public boolean isPinned() {
            return pinned;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CreateClubVote {
        @NotBlank(message = "투표 제목은 필수 입력입니다.")
        @Size(max = 255, message = "투표 제목은 255자 이하로 입력해주세요.")
        private String title;
        @Size(max = 255, message = "투표 내용은 255자 이하로 입력해주세요.")
        private String content;
        @NotNull(message = "투표 항목1은 null이 될 수 없습니다.")
        @Size(max = 255, message = "투표 항목1은 255자 이하로 입력해주세요.")
        private String item1;
        @NotNull(message = "투표 항목2는 null이 될 수 없습니다.")
        @Size(max = 255, message = "투표 항목2는 255자 이하로 입력해주세요.")
        private String item2;
        @Size(max = 255, message = "투표 항목3는 255자 이하로 입력해주세요.")
        private String item3;
        @Size(max = 255, message = "투표 항목4는 255자 이하로 입력해주세요.")
        private String item4;
        @Size(max = 255, message = "투표 항목5는 255자 이하로 입력해주세요.")
        private String item5;
        @Size(max = 255, message = "투표 항목6는 255자 이하로 입력해주세요.")
        private String item6;
        private boolean anonymity;
        private boolean duplication;
        @NotNull(message = "투표 시작 시간은 null이 될 수 없습니다.")
        private LocalDateTime startTime;
        @NotNull(message = "투표 마감 시간은 null이 될 수 없습니다.")
        private LocalDateTime deadline;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateClubNotice {
        @NotBlank(message = "공지사항 제목은 필수 입력입니다.")
        @Size(max = 40, message = "공지사항 제목은 40자 이하로 입력해주세요.")
        private String title;
        @NotNull(message = "공지사항 내용은 null이 될 수 없습니다.")
        @Size(max = 1000, message = "공지사항 내용은 1000자 이하로 입력해주세요.")
        private String content;
        @Getter(AccessLevel.NONE)
        private boolean pinned;
        private Long meetingId;
        @Size(max = 5, message = "공지사항 이미지는 최대 5개까지 가능합니다.")
        private List<@NotBlank(message = "공지사항 이미지 URL은 비어있을 수 없습니다.") String> imageUrls;
        // 미포함/null -> 이미지 변경 X, 빈 리스트 -> 이미지 모두 삭제, 값 있음 -> 이미지 교체
        @Valid
        private UpdateClubVote vote;

        @JsonProperty("isPinned")
        public boolean isPinned() {
            return pinned;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateClubVote {
        @NotNull(message = "투표 마감 시간은 null이 될 수 없습니다.")
        private LocalDateTime deadline;
    }

    @Getter
    @NoArgsConstructor
    public static class VoteResult {
        @NotNull(message = "선택한 투표 항목 번호는 필수입니다.")
        @Size(min = 1, max = 6, message = "투표 항목 번호는 1부터 6 사이여야 합니다.")
        private List<@Min(1) @Max(6) Integer> selectedItemNumbers;

        // 몇 개를 선택했는지 확인하는 DTO용 메서드로, 복수 선택 검증에서 사용됨
        public int countSelectedItems() {
            return selectedItemNumbers == null ? 0 : selectedItemNumbers.size();
        }

        @AssertTrue(message = "선택한 투표 항목 번호는 중복될 수 없습니다.")
        private boolean isSelectedItemNumbersUnique() {
            if (selectedItemNumbers == null) {
                return true;
            }
            return selectedItemNumbers.size() == new HashSet<>(selectedItemNumbers).size();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CreateClubNoticeComment {
        @NotBlank(message = "공지사항 댓글 내용은 필수입니다.")
        private String content;
    }
}
