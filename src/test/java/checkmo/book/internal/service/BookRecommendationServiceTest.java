package checkmo.book.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.service.query.Yes24ApiService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.CheckmoMetrics;
import checkmo.common.monitoring.RecordingSentryCaptureClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestClientException;

class BookRecommendationServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final String REDIS_KEY = RecommendationRefreshGuard.DATA_KEY;
    private static final String REDIS_UPDATED_AT_KEY = RecommendationRefreshGuard.DATE_KEY;
    private static final String SECRET_TTB_KEY = "SECRET_TTB_KEY";
    private static final String SECRET_REFRESH_TOKEN = "SECRET_REFRESH_TOKEN";
    private static final String SECRET_AUTHORIZATION = "Bearer SECRET_AUTHORIZATION";
    private static final String SECRET_COOKIE = "SECRET_COOKIE";

    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private Yes24ApiService yes24ApiService;
    private RecordingSentryCaptureClient captureClient;
    private CheckmoMetrics checkmoMetrics;
    private BookRecommendationService service;
    private RecommendationRefreshGuard guard;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        yes24ApiService = mock(Yes24ApiService.class);
        captureClient = new RecordingSentryCaptureClient();
        checkmoMetrics = new CheckmoMetrics(new SimpleMeterRegistry());
        Yes24RecommendationRefreshClient refreshClient = new Yes24RecommendationRefreshClient(
                yes24ApiService,
                retryProperties(),
                checkmoMetrics,
                backOffPeriod -> {
                }
        );
        guard = mock(RecommendationRefreshGuard.class);
        when(guard.acquire()).thenReturn("token");
        service = new BookRecommendationService(redisTemplate, yes24ApiService, refreshClient, captureClient, checkmoMetrics, guard);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(List.of(REDIS_KEY, REDIS_UPDATED_AT_KEY)))
                .thenAnswer(invocation -> Arrays.asList(valueOperations.get(REDIS_KEY), valueOperations.get(REDIS_UPDATED_AT_KEY)));
    }

    @Test
    void busySharedRefreshReturnsStaleCacheWithoutAnotherProviderCall() {
        var stale = bookList("9788936434595", false);
        when(valueOperations.get(REDIS_KEY)).thenReturn(stale);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn("2000-01-01");
        when(guard.acquire()).thenThrow(new BookException(BookErrorStatus.YES24_API_ERROR));
        when(yes24ApiService.applyLikedByMe(stale, MEMBER_ID)).thenReturn(stale);
        assertThat(service.retrieveRecommendedBooks(MEMBER_ID)).isSameAs(stale);
        verify(yes24ApiService, never()).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
    }

    @Test
    void retrieveRecommendedBooksRetriesTransientYes24AccessFailuresThenSavesTodayRecommendationWhenRefreshEventuallySucceeds() {
        BookResponseDTO.BookList yes24Books = bookListOfSize(4);
        BookException firstFailure = new BookException(
                BookErrorStatus.YES24_API_ERROR,
                new RestClientException("first transient failure")
        );
        BookException secondFailure = new BookException(
                BookErrorStatus.YES24_API_ERROR,
                new IOException("second transient failure")
        );
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt()))
                .thenThrow(firstFailure)
                .thenThrow(secondFailure)
                .thenAnswer(invocation -> BookResponseDTO.BookList.builder()
                        .detailInfoList(List.of(yes24Books.getDetailInfoList().get(
                                List.of("001001046", "001001019", "001001022", "001001007").indexOf(invocation.getArgument(0)))))
                        .hasNext(false).build());
        when(yes24ApiService.applyLikedByMe(any(BookResponseDTO.BookList.class), eq(MEMBER_ID)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

        assertThat(response.getDetailInfoList()).hasSize(4);
        verify(yes24ApiService, times(6)).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
        verify(guard).publish(eq("token"), any(BookResponseDTO.BookList.class), eq(LocalDate.now(ZoneId.of("Asia/Seoul")).toString()));
        verify(guard).release("token");
        verify(valueOperations, never()).set(eq(REDIS_KEY), any(BookResponseDTO.BookList.class), any(Duration.class));
        verify(valueOperations, never()).set(eq(REDIS_UPDATED_AT_KEY), any(String.class), any(Duration.class));
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksCopiesFourSelectedBooksBeforeSavingAndReturning() {
        int startIndex = 0;
        List<DetailInfo> upstreamBooks = mutableDetailInfoList(4);
        DetailInfo originalFirstRecommendation = upstreamBooks.get(startIndex);
        DetailInfo replacementBook = detailInfo("mutated-isbn", "변경된 책");
        BookResponseDTO.BookList yes24Books = BookResponseDTO.BookList.builder()
                .detailInfoList(upstreamBooks)
                .hasNext(false)
                .currentPage(null)
                .totalResults(upstreamBooks.size())
                .build();
        ArgumentCaptor<BookResponseDTO.BookList> savedBookListCaptor =
                ArgumentCaptor.forClass(BookResponseDTO.BookList.class);
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenAnswer(invocation -> BookResponseDTO.BookList.builder()
                        .detailInfoList(List.of(yes24Books.getDetailInfoList().get(
                                List.of("001001046", "001001019", "001001022", "001001007").indexOf(invocation.getArgument(0)))))
                        .hasNext(false).build());
        when(yes24ApiService.applyLikedByMe(any(BookResponseDTO.BookList.class), eq(MEMBER_ID)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);
        upstreamBooks.set(startIndex, replacementBook);

        verify(guard).publish(eq("token"), savedBookListCaptor.capture(), any(String.class));
        BookResponseDTO.BookList savedBookList = savedBookListCaptor.getValue();
        assertThat(response.getDetailInfoList())
                .hasSize(4)
                .first()
                .isSameAs(originalFirstRecommendation);
        assertThat(savedBookList.getDetailInfoList())
                .hasSize(4)
                .first()
                .isSameAs(originalFirstRecommendation);
        assertThat(response.getDetailInfoList()).doesNotContain(replacementBook);
        assertThat(savedBookList.getDetailInfoList()).doesNotContain(replacementBook);
    }

    @Test
    void retrieveRecommendedBooksThrowsBook500AfterThreeFailedRetryAttemptsWhenCacheIsNotUsable() {
        BookException finalFailure = new BookException(
                BookErrorStatus.YES24_API_ERROR,
                new RestClientException("yes24 unavailable")
        );
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenThrow(finalFailure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.YES24_API_ERROR)
                )
                .hasCause(finalFailure);
        verify(yes24ApiService, times(3)).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
        verify(yes24ApiService, never()).applyLikedByMe(any(), any());
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksDoesNotRetryGenericRuntimeFailureWhenCacheIsNotUsable() {
        RuntimeException failure = new RuntimeException("local mapping failure");
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenThrow(failure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.YES24_API_ERROR)
                )
                .hasCause(failure);
        verify(yes24ApiService).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
        verify(yes24ApiService, never()).applyLikedByMe(any(), any());
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksDoesNotRetryBookApiErrorWhenCauseIsGenericRuntimeFailureAndCacheIsNotUsable() {
        BookException failure = new BookException(
                BookErrorStatus.YES24_API_ERROR,
                new RuntimeException("non-transient recommendation failure")
        );
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenThrow(failure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.YES24_API_ERROR)
                )
                .hasCause(failure);
        verify(yes24ApiService).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
        verify(yes24ApiService, never()).applyLikedByMe(any(), any());
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksDoesNotRetryLocalValidationFailureWhenCacheIsNotUsable() {
        IllegalArgumentException failure = new IllegalArgumentException("invalid recommendation request");
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenThrow(failure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.YES24_API_ERROR)
                )
                .hasCause(failure);
        verify(yes24ApiService).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
        verify(yes24ApiService, never()).applyLikedByMe(any(), any());
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksReturnsStaleCacheAndCapturesFailureWhenRefreshFails() {
        BookResponseDTO.BookList staleCache = bookList("9791169213882", false);
        BookResponseDTO.BookList likedStaleCache = bookList("9791169213882", true);
        RuntimeException failure = new RuntimeException("yes24 unavailable");
        when(valueOperations.get(REDIS_KEY)).thenReturn(staleCache);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).toString());
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenThrow(failure);
        when(yes24ApiService.applyLikedByMe(staleCache, MEMBER_ID)).thenReturn(likedStaleCache);

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

        assertThat(response).isSameAs(likedStaleCache);
        assertThat(captureClient.captured()).containsExactly(failure);
        verify(yes24ApiService).applyLikedByMe(staleCache, MEMBER_ID);
    }

    @Test
    void retrieveRecommendedBooksDoesNotLogRawCredentialBearingRefreshFailureWhenReturningStaleCache() {
        BookResponseDTO.BookList staleCache = bookList("9791169213882", false);
        BookResponseDTO.BookList likedStaleCache = bookList("9791169213882", true);
        RuntimeException failure = credentialBearingFailure();
        when(valueOperations.get(REDIS_KEY)).thenReturn(staleCache);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).toString());
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenThrow(failure);
        when(yes24ApiService.applyLikedByMe(staleCache, MEMBER_ID)).thenReturn(likedStaleCache);
        ListAppender<ILoggingEvent> appender = attachServiceLogAppender();

        try {
            BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

            assertThat(response).isSameAs(likedStaleCache);
            assertThat(captureClient.captured()).containsExactly(failure);
            assertRefreshFailureLogsAreSanitized(appender, Level.WARN);
        } finally {
            detachServiceLogAppender(appender);
        }
    }

    @Test
    void refreshDailyRecommendedBooksDoesNotLogRawCredentialBearingRefreshFailureWhenReturningEmptyFallback() {
        RuntimeException failure = credentialBearingFailure();
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenThrow(failure);
        ListAppender<ILoggingEvent> appender = attachServiceLogAppender();

        try {
            BookResponseDTO.BookList response = service.refreshDailyRecommendedBooks();

            assertThat(response.getDetailInfoList()).isEmpty();
            assertThat(captureClient.captured()).containsExactly(failure);
            assertRefreshFailureLogsAreSanitized(appender, Level.ERROR);
        } finally {
            detachServiceLogAppender(appender);
        }
    }

    @Test
    void retrieveRecommendedBooksReturnsStaleCacheAndCapturesFailureWhenRefreshReturnsUnusableBookList() {
        BookResponseDTO.BookList staleCache = bookList("9791169213882", false);
        BookResponseDTO.BookList likedStaleCache = bookList("9791169213882", true);
        when(valueOperations.get(REDIS_KEY)).thenReturn(staleCache);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).toString());
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenReturn(emptyBookList());
        when(yes24ApiService.applyLikedByMe(staleCache, MEMBER_ID)).thenReturn(likedStaleCache);

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

        assertThat(response).isSameAs(likedStaleCache);
        assertThat(captureClient.captured())
                .singleElement()
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.YES24_API_ERROR)
                );
        verify(yes24ApiService).applyLikedByMe(staleCache, MEMBER_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unusableCacheCases")
    void retrieveRecommendedBooksThrowsBook500WhenRefreshFailsAndCacheIsNotUsable(
            String displayName,
            Object cachedData,
            String updatedAt,
            boolean redisReadFails
    ) {
        RuntimeException failure = new RuntimeException("yes24 unavailable");
        if (redisReadFails) {
            when(valueOperations.get(REDIS_KEY)).thenThrow(new IllegalStateException("redis read failed"));
        } else {
            when(valueOperations.get(REDIS_KEY)).thenReturn(cachedData);
            when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(updatedAt);
        }
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenThrow(failure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.YES24_API_ERROR)
                )
                .hasCause(failure);
        assertThat(captureClient.count()).isZero();
        verify(yes24ApiService, never()).applyLikedByMe(any(), any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unusableYes24ResponseCases")
    void retrieveRecommendedBooksThrowsBook500AndDoesNotCaptureWhenNoUsableCacheAndRefreshReturnsUnusableBookList(
            String displayName,
            BookResponseDTO.BookList yes24Response
    ) {
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt())).thenReturn(yes24Response);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.YES24_API_ERROR)
                );
        assertThat(captureClient.count()).isZero();
        verify(yes24ApiService, never()).applyLikedByMe(any(), any());
    }

    @Test
    void retrieveRecommendedBooksReturnsFreshCacheWithoutRefresh() {
        BookResponseDTO.BookList freshCache = bookList("9791169213882", false);
        BookResponseDTO.BookList likedFreshCache = bookList("9791169213882", true);
        when(valueOperations.get(REDIS_KEY)).thenReturn(freshCache);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now(ZoneId.of("Asia/Seoul")).toString());
        when(yes24ApiService.applyLikedByMe(freshCache, MEMBER_ID)).thenReturn(likedFreshCache);

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

        assertThat(response).isSameAs(likedFreshCache);
        verify(yes24ApiService, never()).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
        verify(valueOperations, never()).set(any(String.class), any());
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01-01,2025-12-01,1",
            "2026-03-31,2026-02-01,31",
            "2024-02-29,2024-01-01,29"
    })
    void usesPreviousCalendarMonthAndCurrentDayRank(String today, String previousMonth, int rank) {
        service = spy(service);
        doReturn(LocalDate.parse(today)).when(service).today();
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt()))
                .thenAnswer(invocation -> candidate("isbn-" + invocation.getArgument(0), false));
        when(yes24ApiService.applyLikedByMe(any(), eq(MEMBER_ID))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.retrieveRecommendedBooks(MEMBER_ID);
        assertThat(response.getDetailInfoList()).extracting(DetailInfo::getIsbn)
                .containsExactly("isbn-001001046", "isbn-001001019", "isbn-001001022", "isbn-001001007");
        for (String category : List.of("001001046", "001001019", "001001022", "001001007")) {
            verify(yes24ApiService).retrieveRecommendedBooks(category, LocalDate.parse(previousMonth), rank);
        }
        verify(guard).publish(eq("token"), any(), eq(today));
        verify(service).today();
    }

    @Test
    void skipsDuplicateIsbnAndWrapsMissingRankToFirstPage() {
        service = spy(service);
        doReturn(LocalDate.of(2026, 11, 5)).when(service).today();
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt()))
                .thenAnswer(invocation -> {
                    String category = invocation.getArgument(0);
                    int page = invocation.getArgument(2);
                    if (category.equals("001001019") && page == 5) return candidate("shared", true);
                    if (category.equals("001001022") && page == 5) return emptyBookList();
                    return candidate(category.equals("001001046") ? "shared" : category + page, false);
                });
        when(yes24ApiService.applyLikedByMe(any(), eq(MEMBER_ID))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.retrieveRecommendedBooks(MEMBER_ID).getDetailInfoList())
                .extracting(DetailInfo::getIsbn).containsExactly("shared", "0010010196", "0010010221", "0010010075");
        verify(yes24ApiService, times(6)).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
    }

    @Test
    void stopsAfterFiveCandidatesAndDoesNotPublishPartialRecommendations() {
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(candidate("shared", true));
        assertThat(service.refreshDailyRecommendedBooks().getDetailInfoList()).isEmpty();
        verify(yes24ApiService, times(6)).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
        verify(guard, never()).publish(anyString(), any(), anyString());
        verify(guard).release("token");
    }

    @Test
    void failureInLastCategoryKeepsPreviousFourBooks() {
        var stale = bookList("previous", false);
        when(valueOperations.get(REDIS_KEY)).thenReturn(stale);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn("2000-01-01");
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt()))
                .thenAnswer(invocation -> {
                    String category = invocation.getArgument(0);
                    if (category.equals("001001007")) throw new IllegalArgumentException("failed category");
                    return candidate(category, false);
                });
        when(yes24ApiService.applyLikedByMe(stale, MEMBER_ID)).thenReturn(stale);
        assertThat(service.retrieveRecommendedBooks(MEMBER_ID)).isSameAs(stale);
        verify(guard, never()).publish(anyString(), any(), anyString());
    }

    @Test
    void ignoresPreviousPolicyCacheEvenWhenMarkedToday() {
        when(valueOperations.get("book:recommendations:daily")).thenReturn(bookList("legacy", false));
        when(valueOperations.get("book:recommendations:{yes24}:daily")).thenReturn(bookList("old-yes24", false));
        when(valueOperations.get("book:recommendations:{yes24}:daily:updated_at")).thenReturn(LocalDate.now().toString());
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt()))
                .thenThrow(new IllegalArgumentException("unavailable"));
        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID)).isInstanceOf(BookException.class);
        verify(valueOperations, never()).get("book:recommendations:daily");
        verify(valueOperations, never()).get("book:recommendations:{yes24}:daily");
    }

    @Test
    void stopsWhenWrappingWouldVisitTheSamePageAgain() {
        service = spy(service);
        doReturn(LocalDate.of(2026, 11, 5)).when(service).today();
        when(yes24ApiService.retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(emptyBookList());
        assertThat(service.refreshDailyRecommendedBooks().getDetailInfoList()).isEmpty();
        verify(yes24ApiService).retrieveRecommendedBooks("001001046", LocalDate.of(2026, 10, 1), 5);
        verify(yes24ApiService).retrieveRecommendedBooks("001001046", LocalDate.of(2026, 10, 1), 1);
        verify(yes24ApiService, times(2)).retrieveRecommendedBooks(anyString(), any(LocalDate.class), anyInt());
        verify(guard, never()).publish(anyString(), any(), anyString());
    }

    private static BookResponseDTO.BookList candidate(String isbn, boolean hasNext) {
        return BookResponseDTO.BookList.builder().detailInfoList(List.of(detailInfo(isbn, "책")))
                .hasNext(hasNext).totalResults(100).build();
    }

    private static Stream<Arguments> unusableCacheCases() {
        return Stream.of(
                Arguments.of("missing key is not usable", null, LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).toString(), false),
                Arguments.of("wrong type is not usable", "not a book list", LocalDate.now(ZoneId.of("Asia/Seoul")).toString(), false),
                Arguments.of("null detail info list with today's timestamp is not usable", bookListWithNullDetailInfoList(), LocalDate.now(ZoneId.of("Asia/Seoul")).toString(), false),
                Arguments.of("null detail info list with stale timestamp is not usable", bookListWithNullDetailInfoList(), LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1).toString(), false),
                Arguments.of("empty book list is not usable", emptyBookList(), LocalDate.now(ZoneId.of("Asia/Seoul")).toString(), false),
                Arguments.of("redis read exception is not usable", null, null, true)
        );
    }

    private static Stream<Arguments> unusableYes24ResponseCases() {
        return Stream.of(
                Arguments.of("null yes24 response is not usable", null),
                Arguments.of("empty yes24 response is not usable", emptyBookList()),
                Arguments.of("multiple items for a one-item request are not usable", bookListOfSize(4 - 1))
        );
    }

    private static Yes24Properties retryProperties() {
        Yes24Properties properties = new Yes24Properties();
        properties.getRecommendation().getRefresh().getRetry().setAttempts(3);
        properties.getRecommendation().getRefresh().getRetry().setInitialBackoff(java.time.Duration.ofSeconds(1));
        properties.getRecommendation().getRefresh().getRetry().setMultiplier(2);
        properties.getRecommendation().getRefresh().getRetry().setMaxBackoff(java.time.Duration.ofSeconds(5));
        return properties;
    }

    private static BookResponseDTO.BookList bookList(String isbn, boolean likedByMe) {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(IntStream.range(0, 4).mapToObj(index -> DetailInfo.builder()
                        .isbn(isbn + index).title("테스트 책").likedByMe(likedByMe).build()).toList())
                .hasNext(false).currentPage(null).totalResults(4).build();
    }

    private static BookResponseDTO.BookList bookListOfSize(int size) {
        List<DetailInfo> books = IntStream.range(0, size)
                .mapToObj(index -> detailInfo("isbn-" + index, "테스트 책 " + index))
                .toList();

        return BookResponseDTO.BookList.builder()
                .detailInfoList(books)
                .hasNext(false)
                .currentPage(null)
                .totalResults(size)
                .build();
    }

    private static List<DetailInfo> mutableDetailInfoList(int size) {
        List<DetailInfo> books = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            books.add(detailInfo("isbn-" + index, "테스트 책 " + index));
        }
        return books;
    }

    private static DetailInfo detailInfo(String isbn, String title) {
        return DetailInfo.builder()
                .isbn(isbn)
                .title(title)
                .author("테스트 저자")
                .imgUrl("https://example.com/book-" + isbn + ".jpg")
                .publisher("테스트 출판사")
                .description("테스트 설명")
                .link("https://example.com/book-" + isbn)
                .build();
    }

    private static BookResponseDTO.BookList bookListWithNullDetailInfoList() {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(null)
                .hasNext(false)
                .currentPage(null)
                .totalResults(0)
                .build();
    }

    private static BookResponseDTO.BookList emptyBookList() {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of())
                .hasNext(false)
                .currentPage(null)
                .totalResults(0)
                .build();
    }

    private static RuntimeException credentialBearingFailure() {
        return new RuntimeException(
                "outer failure https://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey="
                        + SECRET_TTB_KEY
                        + "&refresh_token="
                        + SECRET_REFRESH_TOKEN
                        + " Authorization: "
                        + SECRET_AUTHORIZATION,
                new RestClientException(
                        "inner failure https://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey="
                                + SECRET_TTB_KEY
                                + "&Query=java"
                                + " Cookie: "
                                + SECRET_COOKIE
                )
        );
    }

    private static ListAppender<ILoggingEvent> attachServiceLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(BookRecommendationService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private static void detachServiceLogAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(BookRecommendationService.class);
        logger.detachAppender(appender);
        appender.stop();
    }

    private static void assertRefreshFailureLogsAreSanitized(ListAppender<ILoggingEvent> appender, Level level) {
        assertThat(appender.list)
                .filteredOn(event -> event.getLevel().equals(level))
                .anySatisfy(event -> {
                    assertThat(event.getFormattedMessage()).doesNotContain(SECRET_TTB_KEY);
                    assertThat(event.getFormattedMessage()).doesNotContain(SECRET_REFRESH_TOKEN);
                    assertThat(event.getFormattedMessage()).doesNotContain(SECRET_AUTHORIZATION);
                    assertThat(event.getFormattedMessage()).doesNotContain(SECRET_COOKIE);
                    assertThat(event.getFormattedMessage()).doesNotContain("ttbkey=" + SECRET_TTB_KEY);
                    assertThat(event.getFormattedMessage()).contains("ttbkey=***");
                    assertThat(event.getFormattedMessage()).contains("refresh_token=***");
                    assertThat(event.getFormattedMessage()).contains("Authorization: ***");
                    assertThat(event.getFormattedMessage()).contains("Cookie: ***");
                    assertThat(event.getThrowableProxy()).isNull();
                });
    }
}
