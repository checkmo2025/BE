package checkmo.clubManagement.internal.repository.projection;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 클럽의 간단 정보와 추천에 대한 정렬 기준
 */
@Getter
@AllArgsConstructor
public class ClubRecommendation {
    private Long clubId;
    private String clubName;
    private Long overlapCount;
    private Long activeMemberCount;
    private LocalDateTime lastActivityAt;
}
