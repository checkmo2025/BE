package checkmo.clubManagement.internal.repository.projection;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClubSitemapProjection {
    private Long id;
    private LocalDateTime updatedAt;
}
