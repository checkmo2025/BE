package checkmo.news.internal.repository;

import checkmo.news.internal.entity.News;
import checkmo.news.web.dto.NewsResponseDTO;
import java.util.List;

public interface NewsQueryRepository {
    List<News> searchNews(Long cursorId, int pageSize);

    List<News> searchNewsForAdmin(Long cursorId, int pageSize);

    List<News> searchMyNews(String requesterEmail, Long cursorId, int pageSize);

    List<NewsResponseDTO.SitemapItem> findPromotionSitemapItems(Long cursorId, int pageSize);
}
