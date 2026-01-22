package checkmo.news.internal.repository;

import checkmo.news.internal.entity.News;
import java.util.List;

public interface NewsQueryRepository {
    List<News> searchNews(Long cursorId, int pageSize);

    List<News> searchNewsForAdmin(Long cursorId, int pageSize);
}