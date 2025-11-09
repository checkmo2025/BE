package checkmo.clubNotice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubNoticeRequestDTO {
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
            if (item1) {
                count++;
            }
            if (item2) {
                count++;
            }
            if (item3) {
                count++;
            }
            if (item4) {
                count++;
            }
            if (item5) {
                count++;
            }
            return count;
        }
    }
}
