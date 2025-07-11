package checkmo.domain.club.dto.club;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class ClubMemberJoinDTO {
        private String joinMessage;
    }
}
