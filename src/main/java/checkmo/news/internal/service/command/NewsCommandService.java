package checkmo.news.internal.service.command;

import checkmo.news.internal.converter.NewsConverter;
import checkmo.news.internal.entity.News;
import checkmo.news.internal.repository.NewsRepository;
import checkmo.news.web.dto.NewsRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsCommandService {

    private final NewsRepository newsRepository;

    public Long createNews(NewsRequestDTO.CreateNews request) {
        News news = NewsConverter.toNews(request);
        news.replaceImages(request.getImageUrls());

        News savedNews = newsRepository.save(news);
        return savedNews.getId();
    }
}