package checkmo.news.internal.repository;

import checkmo.common.sitemap.SitemapResponseDTO;
import checkmo.news.internal.entity.News;
import java.util.List;

public interface NewsQueryRepository {
    List<News> searchNews(Long cursorId, int pageSize);

    List<News> searchNewsForAdmin(Long cursorId, int pageSize);

    List<News> searchMyNews(String requesterEmail, Long cursorId, int pageSize);

    List<SitemapResponseDTO.Item> findPromotionSitemapItems(Long cursorId, int pageSize);
}
