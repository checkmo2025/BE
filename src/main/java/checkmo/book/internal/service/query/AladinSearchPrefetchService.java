package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.SentryCaptureClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
        prefetchNextPage(keyword, currentPage, null);
    }

    @Async
    public void prefetchNextPage(String keyword, int currentPage, String parentTraceId) {
        int nextPage = currentPage + 1;
        int maxResults = aladinProperties.getSearch().getMaxResults();
        String traceId = parentTraceId == null ? "prefetch-" + nextPage : parentTraceId + "-prefetch-" + nextPage;
        MDC.put(AladinApiService.BOOK_SEARCH_TRACE_ID, traceId);
        long totalStartNanos = System.nanoTime();
        log.info("book-search-timing stage=prefetchStart traceId={} page={}", traceId, nextPage);

        try {
            long cacheRetrieveStartNanos = System.nanoTime();
            if (bookSearchCacheService.retrieve(keyword, maxResults, nextPage).isPresent()) {
                log.info("book-search-timing stage=prefetchEnd traceId={} page={} path=cache-hit cacheRetrieveMs={} totalMs={}",
                        traceId, nextPage, elapsedMillis(cacheRetrieveStartNanos), elapsedMillis(totalStartNanos));
                return;
            }
            log.info("book-search-timing stage=prefetchCacheMiss traceId={} page={} cacheRetrieveMs={}",
                    traceId, nextPage, elapsedMillis(cacheRetrieveStartNanos));

            long aladinFetchStartNanos = System.nanoTime();
            BookResponseDTO.BookList bookList = aladinSearchClient.fetchSearchBooks(keyword, nextPage);
            log.info("book-search-timing stage=prefetchAladinFetch traceId={} page={} elapsedMs={}",
                    traceId, nextPage, elapsedMillis(aladinFetchStartNanos));

            long cacheSaveStartNanos = System.nanoTime();
            bookSearchCacheService.save(keyword, maxResults, nextPage, bookList);
            log.info("book-search-timing stage=prefetchCacheSave traceId={} page={} elapsedMs={}",
                    traceId, nextPage, elapsedMillis(cacheSaveStartNanos));
            log.info("book-search-timing stage=prefetchEnd traceId={} page={} path=saved totalMs={}",
                    traceId, nextPage, elapsedMillis(totalStartNanos));
        } catch (Exception e) {
            log.warn("알라딘 검색 다음 페이지 prefetch 실패. page={}", nextPage, e);
            sentryCaptureClient.captureException(e);
        } finally {
            MDC.remove(AladinApiService.BOOK_SEARCH_TRACE_ID);
        }
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
