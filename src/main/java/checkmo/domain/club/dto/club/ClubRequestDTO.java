package checkmo.domain.club.dto.club;

import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
