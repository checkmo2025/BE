package checkmo.news.internal.service.query;

import checkmo.news.internal.entity.News;
import checkmo.news.internal.exception.NewsErrorStatus;
import checkmo.news.internal.exception.NewsException;
import checkmo.news.internal.repository.NewsRepository;
import checkmo.news.internal.repository.projection.NewsSitemapProjection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsQueryService {

    private final NewsRepository newsRepository;

    public List<News> retrieveNewsList(Long cursorId, int pageSize) {
        return newsRepository.searchNews(cursorId, pageSize);
    }

    public List<News> retrieveNewsListForAdmin(Long cursorId, int pageSize) {
        return newsRepository.searchNewsForAdmin(cursorId, pageSize);
    }

    public List<News> retrieveMyNewsList(String requesterEmail, Long cursorId, int pageSize) {
        return newsRepository.searchMyNews(requesterEmail, cursorId, pageSize);
    }

    public List<NewsSitemapProjection> retrieveSitemapItems(Long cursorId, int pageSize) {
        return newsRepository.findPromotionSitemapItems(cursorId, pageSize);
    }

    public Page<News> retrieveNewsPageForAdmin(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        if (keyword == null || keyword.isBlank()) {
            return newsRepository.findAll(pageable);
        }

        return newsRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public News retrieveNews(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsException(NewsErrorStatus.NEWS_NOT_FOUND));
    }
}
