package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.SentryCaptureClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AladinSearchPrefetchService {

    private final AladinProperties aladinProperties;
    private final BookSearchCacheService bookSearchCacheService;
    private final AladinSearchClient aladinSearchClient;
    private final SentryCaptureClient sentryCaptureClient;

    @Async
    public void prefetchNextPage(String keyword, int currentPage) {
        int nextPage = currentPage + 1;
        int maxResults = aladinProperties.getSearch().getMaxResults();

        try {
            if (bookSearchCacheService.retrieve(keyword, maxResults, nextPage).isPresent()) {
                return;
            }

            BookResponseDTO.BookList bookList = aladinSearchClient.fetchSearchBooks(keyword, nextPage);
            bookSearchCacheService.save(keyword, maxResults, nextPage, bookList);
        } catch (Exception e) {
            log.warn("알라딘 검색 다음 페이지 prefetch 실패. page={}", nextPage, e);
            sentryCaptureClient.captureException(e);
        }
    }
}
