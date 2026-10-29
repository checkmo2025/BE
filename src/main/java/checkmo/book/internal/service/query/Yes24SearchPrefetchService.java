package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.SentryCaptureClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Yes24SearchPrefetchService {

    private final Yes24Properties yes24Properties;
    private final BookSearchCacheService bookSearchCacheService;
    private final Yes24SearchClient yes24SearchClient;
    private final SentryCaptureClient sentryCaptureClient;

    @Async
    public void prefetchNextPage(String keyword, int currentPage) {
        int nextPage = currentPage + 1;
        int maxResults = yes24Properties.getSearch().getMaxResults();

        try {
            if (bookSearchCacheService.retrieve(keyword, maxResults, nextPage).isPresent()) {
                return;
            }

            BookResponseDTO.BookList bookList = yes24SearchClient.fetchSearchBooks(keyword, nextPage);
            bookSearchCacheService.save(keyword, maxResults, nextPage, bookList);
        } catch (Exception e) {
            log.warn("알라딘 검색 다음 페이지 prefetch 실패. page={}", nextPage, e);
            sentryCaptureClient.captureException(e);
        }
    }
}
