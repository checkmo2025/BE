package checkmo.news.internal.service.query;

import checkmo.news.internal.entity.News;
import checkmo.news.internal.repository.NewsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
}