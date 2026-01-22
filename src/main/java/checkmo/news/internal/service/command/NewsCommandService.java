package checkmo.news.internal.service.command;

import checkmo.news.internal.converter.NewsConverter;
import checkmo.news.internal.entity.News;
import checkmo.news.internal.exception.NewsErrorStatus;
import checkmo.news.internal.exception.NewsException;
import checkmo.news.internal.repository.NewsRepository;
import checkmo.news.web.dto.NewsRequestDTO;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsCommandService {

    private final NewsRepository newsRepository;

    public Long createNews(NewsRequestDTO.CreateNews request) {
        validatePublishRange(request.getPublishStartAt(), request.getPublishEndAt());

        News news = NewsConverter.toNews(request);
        news.replaceImages(request.getImageUrls());

        News savedNews = newsRepository.save(news);
        return savedNews.getId();
    }

    public Long updateNews(Long newsId, NewsRequestDTO.UpdateNews request) {
        validatePublishRange(request.getPublishStartAt(), request.getPublishEndAt());

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsException(NewsErrorStatus.NEWS_NOT_FOUND));

        news.update(
                request.getTitle(),
                request.getRequesterEmail(),
                request.getContent(),
                request.getThumbnailUrl(),
                request.getOriginalLink(),
                request.getPublishStartAt(),
                request.getPublishEndAt()
        );
        news.replaceImages(request.getImageUrls());

        return news.getId();
    }

    public void deleteNews(Long newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NewsException(NewsErrorStatus.NEWS_NOT_FOUND));

        newsRepository.delete(news);
    }

    private void validatePublishRange(LocalDate publishStartAt, LocalDate publishEndAt) {
        if (publishStartAt != null && publishEndAt != null && publishStartAt.isAfter(publishEndAt)) {
            throw new NewsException(NewsErrorStatus.INVALID_PUBLISH_RANGE);
        }
    }
}