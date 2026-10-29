package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.SentryCaptureClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Yes24SearchPrefetchServiceTest {

    private Yes24Properties yes24Properties;
    private BookSearchCacheService bookSearchCacheService;
    private Yes24SearchClient yes24SearchClient;
    private SentryCaptureClient sentryCaptureClient;
    private Yes24SearchPrefetchService yes24SearchPrefetchService;

    @BeforeEach
    void setUp() {
        yes24Properties = new Yes24Properties();
        yes24Properties.getSearch().setMaxResults(10);
        bookSearchCacheService = mock(BookSearchCacheService.class);
        yes24SearchClient = mock(Yes24SearchClient.class);
        sentryCaptureClient = mock(SentryCaptureClient.class);
        yes24SearchPrefetchService = new Yes24SearchPrefetchService(
                yes24Properties,
                bookSearchCacheService,
                yes24SearchClient,
                sentryCaptureClient
        );
    }

    @Test
    void prefetchNextPageCachesOnlyCurrentPagePlusOne() {
        BookResponseDTO.BookList page2 = bookList(2, true);
        when(bookSearchCacheService.retrieve("java", 10, 2)).thenReturn(Optional.empty());
        when(yes24SearchClient.fetchSearchBooks("java", 2)).thenReturn(page2);

        yes24SearchPrefetchService.prefetchNextPage("java", 1);

        verify(yes24SearchClient).fetchSearchBooks("java", 2);
        verify(bookSearchCacheService).save("java", 10, 2, page2);
        verify(yes24SearchClient, never()).fetchSearchBooks("java", 3);
    }

    @Test
    void prefetchNextPageDoesNothingWhenNextPageAlreadyCached() {
        when(bookSearchCacheService.retrieve("java", 10, 2)).thenReturn(Optional.of(bookList(2, true)));

        yes24SearchPrefetchService.prefetchNextPage("java", 1);

        verify(yes24SearchClient, never()).fetchSearchBooks("java", 2);
        verify(bookSearchCacheService, never()).save(eq("java"), eq(10), eq(2), any());
    }

    @Test
    void prefetchNextPageCapturesFailureWithoutThrowing() {
        RuntimeException failure = new RuntimeException("aladin unavailable");
        when(bookSearchCacheService.retrieve("java", 10, 2)).thenReturn(Optional.empty());
        when(yes24SearchClient.fetchSearchBooks("java", 2)).thenThrow(failure);

        assertThatCode(() -> yes24SearchPrefetchService.prefetchNextPage("java", 1))
                .doesNotThrowAnyException();

        verify(sentryCaptureClient).captureException(failure);
    }

    private BookResponseDTO.BookList bookList(int page, boolean hasNext) {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of(DetailInfo.builder()
                        .isbn("9791169213882")
                        .title("테스트 책")
                        .author("테스트 저자")
                        .imgUrl("https://example.com/book.jpg")
                        .publisher("테스트 출판사")
                        .description("테스트 설명")
                        .link("https://example.com/book")
                        .build()))
                .hasNext(hasNext)
                .currentPage(page)
                .totalResults(37)
                .build();
    }
}
