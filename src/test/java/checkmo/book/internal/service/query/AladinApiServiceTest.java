package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.repository.BookLikedRepository;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.SentryCaptureClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.web.client.RestTemplate;

class AladinApiServiceTest {

    private RestTemplate restTemplate;
    private AladinProperties aladinProperties;
    private BookLikedRepository bookLikedRepository;
    private BookSearchCacheService bookSearchCacheService;
    private AladinSearchClient aladinSearchClient;
    private AladinSearchPrefetchService aladinSearchPrefetchService;
    private SentryCaptureClient sentryCaptureClient;
    private AladinApiService aladinApiService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        aladinProperties = aladinProperties();
        bookLikedRepository = mock(BookLikedRepository.class, Answers.CALLS_REAL_METHODS);
        bookSearchCacheService = mock(BookSearchCacheService.class);
        aladinSearchClient = mock(AladinSearchClient.class);
        aladinSearchPrefetchService = mock(AladinSearchPrefetchService.class);
        sentryCaptureClient = mock(SentryCaptureClient.class);
        aladinApiService = new AladinApiService(
                restTemplate,
                aladinProperties,
                bookLikedRepository,
                bookSearchCacheService,
                aladinSearchClient,
                aladinSearchPrefetchService,
                sentryCaptureClient
        );
    }

    @Test
    void applyLikedByMePreservesSearchMetadata() {
        when(bookLikedRepository.findLikedBookIds(eq(1L), anyList()))
                .thenReturn(List.of("9791169213882"));

        BookResponseDTO.BookList response = aladinApiService.applyLikedByMe(bookList(false, true), 1L);

        assertThat(response.getTotalResults()).isEqualTo(37);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getDetailInfoList()).singleElement()
                .extracting(DetailInfo::isLikedByMe)
                .isEqualTo(true);
    }

    @Test
    void searchBooksUsesCachedRawResultAndAppliesLikedByMe() {
        when(bookSearchCacheService.retrieve("java", 10, 1)).thenReturn(Optional.of(bookList(false, true)));
        when(bookLikedRepository.findLikedBookIds(eq(1L), anyList()))
                .thenReturn(List.of("9791169213882"));

        BookResponseDTO.BookList response = aladinApiService.retrieveSearchBooks("java", 1, 1L);

        verify(aladinSearchClient, never()).fetchSearchBooks("java", 1);
        assertThat(response.getTotalResults()).isEqualTo(37);
        assertThat(response.getDetailInfoList()).singleElement()
                .extracting(DetailInfo::isLikedByMe)
                .isEqualTo(true);
    }

    @Test
    void searchBooksReturnsEmptyResultWhenKeywordIsBlank() {
        BookResponseDTO.BookList response = aladinApiService.retrieveSearchBooks("   ", 1, 1L);

        assertThat(response.getTotalResults()).isZero();
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getDetailInfoList()).isEmpty();
        verifyNoInteractions(
                bookLikedRepository,
                bookSearchCacheService,
                aladinSearchClient,
                aladinSearchPrefetchService,
                sentryCaptureClient
        );
    }

    @Test
    void searchBooksCachesSuccessfulRawResultAndPrefetchesNextPage() {
        BookResponseDTO.BookList rawBookList = bookList(false, true);
        when(bookSearchCacheService.retrieve("java", 10, 1)).thenReturn(Optional.empty());
        when(aladinSearchClient.fetchSearchBooks("java", 1)).thenReturn(rawBookList);
        when(bookLikedRepository.findLikedBookIds(eq(1L), anyList()))
                .thenReturn(List.of("9791169213882"));

        BookResponseDTO.BookList response = aladinApiService.retrieveSearchBooks("java", 1, 1L);

        verify(bookSearchCacheService).save("java", 10, 1, rawBookList);
        verify(aladinSearchPrefetchService).prefetchNextPage("java", 1);
        assertThat(response.getDetailInfoList()).singleElement()
                .extracting(DetailInfo::isLikedByMe)
                .isEqualTo(true);
    }

    @Test
    void searchBooksDoesNotPrefetchWhenHasNextIsFalse() {
        BookResponseDTO.BookList rawBookList = bookList(false, false);
        when(bookSearchCacheService.retrieve("java", 10, 1)).thenReturn(Optional.empty());
        when(aladinSearchClient.fetchSearchBooks("java", 1)).thenReturn(rawBookList);

        aladinApiService.retrieveSearchBooks("java", 1, 1L);

        verify(aladinSearchPrefetchService, never()).prefetchNextPage("java", 1);
    }

    @Test
    void searchBooksReturnsCachedFallbackAndCapturesAladinFailure() {
        RuntimeException failure = new RuntimeException("aladin unavailable");
        when(bookSearchCacheService.retrieve("java", 10, 1))
                .thenReturn(Optional.empty(), Optional.of(bookList(false, false)));
        when(aladinSearchClient.fetchSearchBooks("java", 1)).thenThrow(failure);
        when(bookLikedRepository.findLikedBookIds(eq(1L), anyList()))
                .thenReturn(List.of("9791169213882"));

        BookResponseDTO.BookList response = aladinApiService.retrieveSearchBooks("java", 1, 1L);

        verify(sentryCaptureClient).captureException(failure);
        assertThat(response.getTotalResults()).isEqualTo(37);
        assertThat(response.getDetailInfoList()).singleElement()
                .extracting(DetailInfo::isLikedByMe)
                .isEqualTo(true);
    }

    @Test
    void searchBooksThrowsBook500WhenAladinFailsAndCacheMissing() {
        RuntimeException failure = new RuntimeException("aladin unavailable");
        when(bookSearchCacheService.retrieve("java", 10, 1)).thenReturn(Optional.empty());
        when(aladinSearchClient.fetchSearchBooks("java", 1)).thenThrow(failure);

        assertThatThrownBy(() -> aladinApiService.retrieveSearchBooks("java", 1, 1L))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                )
                .hasCause(failure);
    }

    private AladinProperties aladinProperties() {
        AladinProperties properties = new AladinProperties();
        properties.getSearch().setMaxResults(10);
        return properties;
    }

    private BookResponseDTO.BookList bookList(boolean likedByMe, boolean hasNext) {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of(DetailInfo.builder()
                        .isbn("9791169213882")
                        .title("테스트 책")
                        .author("테스트 저자")
                        .imgUrl("https://example.com/book.jpg")
                        .publisher("테스트 출판사")
                        .description("테스트 설명")
                        .link("https://example.com/book")
                        .likedByMe(likedByMe)
                        .build()))
                .hasNext(hasNext)
                .currentPage(2)
                .totalResults(37)
                .build();
    }
}
