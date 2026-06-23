package checkmo.news.internal.repository.projection;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewsSitemapProjection {
    private Long id;
    private LocalDateTime updatedAt;
}
