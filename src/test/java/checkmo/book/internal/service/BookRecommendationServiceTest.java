package checkmo.book.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.internal.util.DayOfWeekUtils;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.RecordingSentryCaptureClient;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestClientException;

class BookRecommendationServiceTest {

    private static final String MEMBER_ID = "member-1";
    private static final String REDIS_KEY = "book:recommendations:daily";
    private static final String REDIS_UPDATED_AT_KEY = "book:recommendations:daily:updated_at";
    private static final String SECRET_TTB_KEY = "SECRET_TTB_KEY";
    private static final String SECRET_REFRESH_TOKEN = "SECRET_REFRESH_TOKEN";
    private static final String SECRET_AUTHORIZATION = "Bearer SECRET_AUTHORIZATION";
    private static final String SECRET_COOKIE = "SECRET_COOKIE";

    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private AladinApiService aladinApiService;
    private RecordingSentryCaptureClient captureClient;
    private BookRecommendationService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        aladinApiService = mock(AladinApiService.class);
        captureClient = new RecordingSentryCaptureClient();
        AladinRecommendationRefreshClient refreshClient = new AladinRecommendationRefreshClient(
                aladinApiService,
                retryProperties(),
                backOffPeriod -> {
                }
        );
        service = new BookRecommendationService(redisTemplate, aladinApiService, refreshClient, captureClient);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void retrieveRecommendedBooksRetriesTransientAladinAccessFailuresThenSavesTodayRecommendationWhenRefreshEventuallySucceeds() {
        BookResponseDTO.BookList aladinBooks = bookListOfSize(todayRequiredBookCount());
        BookException firstFailure = new BookException(
                BookErrorStatus.ALADIN_API_ERROR,
                new RestClientException("first transient failure")
        );
        BookException secondFailure = new BookException(
                BookErrorStatus.ALADIN_API_ERROR,
                new IOException("second transient failure")
        );
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(aladinApiService.retrieveRecommendedBooks())
                .thenThrow(firstFailure)
                .thenThrow(secondFailure)
                .thenReturn(aladinBooks);
        when(aladinApiService.applyLikedByMe(any(BookResponseDTO.BookList.class), eq(MEMBER_ID)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

        assertThat(response.getDetailInfoList()).hasSize(4);
        verify(aladinApiService, times(3)).retrieveRecommendedBooks();
        verify(valueOperations).set(eq(REDIS_KEY), any(BookResponseDTO.BookList.class));
        verify(valueOperations).set(REDIS_UPDATED_AT_KEY, LocalDate.now().toString());
        verify(valueOperations, never()).set(eq(REDIS_KEY), any(BookResponseDTO.BookList.class), any(Duration.class));
        verify(valueOperations, never()).set(eq(REDIS_UPDATED_AT_KEY), any(String.class), any(Duration.class));
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksCopiesTodayRecommendationSliceBeforeSavingAndReturning() {
        int startIndex = DayOfWeekUtils.calculateStartIndexByDayOfWeek();
        List<DetailInfo> upstreamBooks = mutableDetailInfoList(todayRequiredBookCount());
        DetailInfo originalFirstRecommendation = upstreamBooks.get(startIndex);
        DetailInfo replacementBook = detailInfo("mutated-isbn", "변경된 책");
        BookResponseDTO.BookList aladinBooks = BookResponseDTO.BookList.builder()
                .detailInfoList(upstreamBooks)
                .hasNext(false)
                .currentPage(null)
                .totalResults(upstreamBooks.size())
                .build();
        ArgumentCaptor<BookResponseDTO.BookList> savedBookListCaptor =
                ArgumentCaptor.forClass(BookResponseDTO.BookList.class);
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(aladinApiService.retrieveRecommendedBooks()).thenReturn(aladinBooks);
        when(aladinApiService.applyLikedByMe(any(BookResponseDTO.BookList.class), eq(MEMBER_ID)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);
        upstreamBooks.set(startIndex, replacementBook);

        verify(valueOperations).set(eq(REDIS_KEY), savedBookListCaptor.capture());
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
                BookErrorStatus.ALADIN_API_ERROR,
                new RestClientException("aladin unavailable")
        );
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(finalFailure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                )
                .hasCause(finalFailure);
        verify(aladinApiService, times(3)).retrieveRecommendedBooks();
        verify(aladinApiService, never()).applyLikedByMe(any(), any());
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksDoesNotRetryGenericRuntimeFailureWhenCacheIsNotUsable() {
        RuntimeException failure = new RuntimeException("local mapping failure");
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(failure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                )
                .hasCause(failure);
        verify(aladinApiService).retrieveRecommendedBooks();
        verify(aladinApiService, never()).applyLikedByMe(any(), any());
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksDoesNotRetryBookApiErrorWhenCauseIsGenericRuntimeFailureAndCacheIsNotUsable() {
        BookException failure = new BookException(
                BookErrorStatus.ALADIN_API_ERROR,
                new RuntimeException("non-transient recommendation failure")
        );
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(failure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                )
                .hasCause(failure);
        verify(aladinApiService).retrieveRecommendedBooks();
        verify(aladinApiService, never()).applyLikedByMe(any(), any());
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksDoesNotRetryLocalValidationFailureWhenCacheIsNotUsable() {
        IllegalArgumentException failure = new IllegalArgumentException("invalid recommendation request");
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(failure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                )
                .hasCause(failure);
        verify(aladinApiService).retrieveRecommendedBooks();
        verify(aladinApiService, never()).applyLikedByMe(any(), any());
        assertThat(captureClient.count()).isZero();
    }

    @Test
    void retrieveRecommendedBooksReturnsStaleCacheAndCapturesFailureWhenRefreshFails() {
        BookResponseDTO.BookList staleCache = bookList("9791169213882", false);
        BookResponseDTO.BookList likedStaleCache = bookList("9791169213882", true);
        RuntimeException failure = new RuntimeException("aladin unavailable");
        when(valueOperations.get(REDIS_KEY)).thenReturn(staleCache);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now().minusDays(1).toString());
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(failure);
        when(aladinApiService.applyLikedByMe(staleCache, MEMBER_ID)).thenReturn(likedStaleCache);

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

        assertThat(response).isSameAs(likedStaleCache);
        assertThat(captureClient.captured()).containsExactly(failure);
        verify(aladinApiService).applyLikedByMe(staleCache, MEMBER_ID);
    }

    @Test
    void retrieveRecommendedBooksDoesNotLogRawCredentialBearingRefreshFailureWhenReturningStaleCache() {
        BookResponseDTO.BookList staleCache = bookList("9791169213882", false);
        BookResponseDTO.BookList likedStaleCache = bookList("9791169213882", true);
        RuntimeException failure = credentialBearingFailure();
        when(valueOperations.get(REDIS_KEY)).thenReturn(staleCache);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now().minusDays(1).toString());
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(failure);
        when(aladinApiService.applyLikedByMe(staleCache, MEMBER_ID)).thenReturn(likedStaleCache);
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
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(failure);
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
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now().minusDays(1).toString());
        when(aladinApiService.retrieveRecommendedBooks()).thenReturn(emptyBookList());
        when(aladinApiService.applyLikedByMe(staleCache, MEMBER_ID)).thenReturn(likedStaleCache);

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

        assertThat(response).isSameAs(likedStaleCache);
        assertThat(captureClient.captured())
                .singleElement()
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                );
        verify(aladinApiService).applyLikedByMe(staleCache, MEMBER_ID);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unusableCacheCases")
    void retrieveRecommendedBooksThrowsBook500WhenRefreshFailsAndCacheIsNotUsable(
            String displayName,
            Object cachedData,
            String updatedAt,
            boolean redisReadFails
    ) {
        RuntimeException failure = new RuntimeException("aladin unavailable");
        if (redisReadFails) {
            when(valueOperations.get(REDIS_KEY)).thenThrow(new IllegalStateException("redis read failed"));
        } else {
            when(valueOperations.get(REDIS_KEY)).thenReturn(cachedData);
            when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(updatedAt);
        }
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(failure);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                )
                .hasCause(failure);
        assertThat(captureClient.count()).isZero();
        verify(aladinApiService, never()).applyLikedByMe(any(), any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unusableAladinResponseCases")
    void retrieveRecommendedBooksThrowsBook500AndDoesNotCaptureWhenNoUsableCacheAndRefreshReturnsUnusableBookList(
            String displayName,
            BookResponseDTO.BookList aladinResponse
    ) {
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(aladinApiService.retrieveRecommendedBooks()).thenReturn(aladinResponse);

        assertThatThrownBy(() -> service.retrieveRecommendedBooks(MEMBER_ID))
                .isInstanceOfSatisfying(BookException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR)
                );
        assertThat(captureClient.count()).isZero();
        verify(aladinApiService, never()).applyLikedByMe(any(), any());
    }

    @Test
    void retrieveRecommendedBooksReturnsFreshCacheWithoutRefresh() {
        BookResponseDTO.BookList freshCache = bookList("9791169213882", false);
        BookResponseDTO.BookList likedFreshCache = bookList("9791169213882", true);
        when(valueOperations.get(REDIS_KEY)).thenReturn(freshCache);
        when(valueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now().toString());
        when(aladinApiService.applyLikedByMe(freshCache, MEMBER_ID)).thenReturn(likedFreshCache);

        BookResponseDTO.BookList response = service.retrieveRecommendedBooks(MEMBER_ID);

        assertThat(response).isSameAs(likedFreshCache);
        verify(aladinApiService, never()).retrieveRecommendedBooks();
        verify(valueOperations, never()).set(any(String.class), any());
    }

    private static Stream<Arguments> unusableCacheCases() {
        return Stream.of(
                Arguments.of("missing key is not usable", null, LocalDate.now().minusDays(1).toString(), false),
                Arguments.of("wrong type is not usable", "not a book list", LocalDate.now().toString(), false),
                Arguments.of("null detail info list with today's timestamp is not usable", bookListWithNullDetailInfoList(), LocalDate.now().toString(), false),
                Arguments.of("null detail info list with stale timestamp is not usable", bookListWithNullDetailInfoList(), LocalDate.now().minusDays(1).toString(), false),
                Arguments.of("empty book list is not usable", emptyBookList(), LocalDate.now().toString(), false),
                Arguments.of("redis read exception is not usable", null, null, true)
        );
    }

    private static Stream<Arguments> unusableAladinResponseCases() {
        return Stream.of(
                Arguments.of("null aladin response is not usable", null),
                Arguments.of("empty aladin response is not usable", emptyBookList()),
                Arguments.of("fewer than today's four-book window is not usable", bookListOfSize(todayRequiredBookCount() - 1))
        );
    }

    private static AladinProperties retryProperties() {
        AladinProperties properties = new AladinProperties();
        properties.getRecommendation().getRefresh().getRetry().setAttempts(3);
        properties.getRecommendation().getRefresh().getRetry().setInitialBackoff(java.time.Duration.ofSeconds(1));
        properties.getRecommendation().getRefresh().getRetry().setMultiplier(2);
        properties.getRecommendation().getRefresh().getRetry().setMaxBackoff(java.time.Duration.ofSeconds(5));
        return properties;
    }

    private static int todayRequiredBookCount() {
        return DayOfWeekUtils.calculateStartIndexByDayOfWeek() + 4;
    }

    private static BookResponseDTO.BookList bookList(String isbn, boolean likedByMe) {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of(DetailInfo.builder()
                        .isbn(isbn)
                        .title("테스트 책")
                        .author("테스트 저자")
                        .imgUrl("https://example.com/book.jpg")
                        .publisher("테스트 출판사")
                        .description("테스트 설명")
                        .link("https://example.com/book")
                        .likedByMe(likedByMe)
                        .build()))
                .hasNext(false)
                .currentPage(null)
                .totalResults(1)
                .build();
    }

    private static BookResponseDTO.BookList bookListOfSize(int size) {
        List<DetailInfo> books = java.util.stream.IntStream.range(0, size)
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
