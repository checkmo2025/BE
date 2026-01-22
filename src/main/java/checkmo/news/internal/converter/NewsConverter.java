package checkmo.news.internal.converter;

import checkmo.news.internal.entity.News;
import checkmo.news.web.dto.NewsRequestDTO;
import checkmo.news.web.dto.NewsResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsConverter {

    private static final int DESCRIPTION_MAX_LENGTH = 30;

    public static News toNews(NewsRequestDTO.CreateNews request) {
        return News.builder()
                .title(request.getTitle())
                .requesterEmail(request.getRequesterEmail())
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .originalLink(request.getOriginalLink())
                .publishStartAt(request.getPublishStartAt())
                .publishEndAt(request.getPublishEndAt())
                .build();
    }

    public static NewsResponseDTO.BasicInfo toBasicInfo(News news) {
        return NewsResponseDTO.BasicInfo.builder()
                .newsId(news.getId())
                .title(news.getTitle())
                .description(truncateDescription(news.getContent()))
                .thumbnailUrl(news.getThumbnailUrl())
                .publishStartAt(news.getPublishStartAt())
                .build();
    }

    private static String truncateDescription(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= DESCRIPTION_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, DESCRIPTION_MAX_LENGTH);
    }
}