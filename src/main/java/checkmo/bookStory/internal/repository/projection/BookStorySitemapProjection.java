package checkmo.bookStory.internal.repository.projection;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookStorySitemapProjection {
    private Long id;
    private LocalDateTime updatedAt;
}
