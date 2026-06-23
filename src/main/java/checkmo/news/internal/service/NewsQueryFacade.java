package checkmo.news.internal.service;

import checkmo.common.sitemap.SitemapResponseDTO;
import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.news.internal.converter.NewsConverter;
import checkmo.news.internal.entity.News;
import checkmo.news.internal.exception.NewsErrorStatus;
import checkmo.news.internal.exception.NewsException;
import checkmo.news.internal.service.query.NewsQueryService;
import checkmo.news.web.dto.NewsResponseDTO;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsQueryFacade {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int ADMIN_PAGE_SIZE = 12;

    private final NewsQueryService newsQueryService;
    private final MemberAPI memberAPI;

    public NewsResponseDTO.NewsList fetchNewsList(Long cursorId) {
        CursorResult<News> newsCursorResult = CursorPagingHelper.getPage(
                (pageSize) -> newsQueryService.retrieveNewsList(cursorId, pageSize),
                News::getId,
                DEFAULT_PAGE_SIZE
        );

        List<NewsResponseDTO.BasicInfo> basicInfoList = newsCursorResult.content().stream()
                .map(NewsConverter::toBasicInfo)
                .toList();

        return NewsResponseDTO.NewsList.builder()
                .basicInfoList(basicInfoList)
                .hasNext(newsCursorResult.hasNext())
                .nextCursor(newsCursorResult.nextCursor())
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
    }

    public NewsResponseDTO.DetailInfo fetchNewsDetail(Long newsId) {
        News news = newsQueryService.retrieveNews(newsId);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        if (!news.isPublished(today)) {
            throw new NewsException(NewsErrorStatus.NEWS_NOT_PUBLISHED);
        }

        return NewsConverter.toDetailInfo(news);
    }

    public NewsResponseDTO.NewsList fetchMyNewsList(String memberId, Long cursorId) {
        String requesterEmail = memberAPI.fetchMemberEmail(memberId);

        CursorResult<News> newsCursorResult = CursorPagingHelper.getPage(
                (pageSize) -> newsQueryService.retrieveMyNewsList(requesterEmail, cursorId, pageSize),
                News::getId,
                DEFAULT_PAGE_SIZE
        );

        List<NewsResponseDTO.BasicInfo> basicInfoList = newsCursorResult.content().stream()
                .map(NewsConverter::toBasicInfo)
                .toList();

        return NewsResponseDTO.NewsList.builder()
                .basicInfoList(basicInfoList)
                .hasNext(newsCursorResult.hasNext())
                .nextCursor(newsCursorResult.nextCursor())
                .pageSize(DEFAULT_PAGE_SIZE)
                .build();
    }

    public SitemapResponseDTO.Page fetchNewsSitemap(Long cursorId, Integer limit) {
        int pageSize = SitemapResponseDTO.normalizeLimit(limit);
        CursorResult<SitemapResponseDTO.Item> sitemapCursorResult = CursorPagingHelper.getPage(
                requestedPageSize -> newsQueryService.retrieveSitemapItems(cursorId, requestedPageSize),
                SitemapResponseDTO.Item::id,
                pageSize
        );

        return SitemapResponseDTO.Page.from(sitemapCursorResult, pageSize);
    }

    public NewsResponseDTO.AdminNewsList fetchNewsListForAdmin(String keyword, int page) {
        Page<News> newsPage = newsQueryService.retrieveNewsPageForAdmin(keyword, page, ADMIN_PAGE_SIZE);

        List<NewsResponseDTO.AdminBasicInfo> basicInfoList = newsPage.getContent().stream()
                .map(NewsConverter::toAdminBasicInfo)
                .toList();

        return NewsResponseDTO.AdminNewsList.builder()
                .basicInfoList(basicInfoList)
                .page(page)
                .totalPages(newsPage.getTotalPages())
                .totalElements(newsPage.getTotalElements())
                .build();
    }

    public NewsResponseDTO.AdminDetailInfo fetchNewsDetailForAdmin(Long newsId) {
        News news = newsQueryService.retrieveNews(newsId);
        return NewsConverter.toAdminDetailInfo(news);
    }

    public NewsResponseDTO.NewsList fetchMemberNewsListForAdmin(String memberNickname, Long cursorId) {
        String memberId = memberAPI.fetchMemberId(memberNickname);
        return fetchMyNewsList(memberId, cursorId);
    }
}
