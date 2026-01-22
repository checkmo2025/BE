package checkmo.news.internal.converter;

import checkmo.news.internal.entity.News;
import checkmo.news.web.dto.NewsRequestDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsConverter {

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
}