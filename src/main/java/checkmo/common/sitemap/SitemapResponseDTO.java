package checkmo.common.sitemap;

import checkmo.common.template.CursorResult;
import java.time.LocalDateTime;
import java.util.List;

public class SitemapResponseDTO {

    public static final int DEFAULT_SITEMAP_LIMIT = 1000;
    public static final int MAX_SITEMAP_LIMIT = 5000;

    private SitemapResponseDTO() {
    }

    public static int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_SITEMAP_LIMIT;
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Sitemap limit must be at least 1.");
        }
        return Math.min(limit, MAX_SITEMAP_LIMIT);
    }

    public record Item(
            Long id,
            LocalDateTime updatedAt
    ) {
    }

    public record Page(
            List<Item> items,
            boolean hasNext,
            Long nextCursor,
            int pageSize
    ) {

        public static Page from(CursorResult<Item> cursorResult, int pageSize) {
            return new Page(
                    cursorResult.content(),
                    cursorResult.hasNext(),
                    cursorResult.nextCursor(),
                    pageSize
            );
        }
    }
}
