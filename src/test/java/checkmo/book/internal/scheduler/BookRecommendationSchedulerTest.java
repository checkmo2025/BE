package checkmo.book.internal.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import checkmo.book.internal.service.BookRecommendationService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.RecordingSentryCaptureClient;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.LoggerFactory;

class BookRecommendationSchedulerTest {

    private static final String SECRET_TTB_KEY = "SECRET_TTB_KEY";
    private static final String SECRET_REFRESH_TOKEN = "SECRET_REFRESH_TOKEN";
    private static final String SECRET_AUTHORIZATION = "Bearer SECRET_AUTHORIZATION";
    private static final String SECRET_COOKIE = "SECRET_COOKIE";

    private BookRecommendationService recommendationService;
    private RecordingSentryCaptureClient captureClient;
    private BookRecommendationScheduler scheduler;

    @BeforeEach
    void setUp() {
        recommendationService = mock(BookRecommendationService.class);
        captureClient = new RecordingSentryCaptureClient();
        scheduler = new BookRecommendationScheduler(recommendationService, captureClient);
    }

    @Test
    void retryRecommendedBooksRefreshRefreshesWhenCacheIsMissing() {
        when(recommendationService.hasRecommendedBooks()).thenReturn(false);
        when(recommendationService.refreshDailyRecommendedBooks()).thenReturn(bookList());

        scheduler.retryRecommendedBooksRefresh();

        verify(recommendationService).refreshDailyRecommendedBooks();
    }

    @Test
    void retryRecommendedBooksRefreshRefreshesWhenCacheIsStale() {
        when(recommendationService.hasRecommendedBooks()).thenReturn(true);
        when(recommendationService.isRecommendedBooksStale()).thenReturn(true);
        when(recommendationService.refreshDailyRecommendedBooks()).thenReturn(bookList());

        scheduler.retryRecommendedBooksRefresh();

        verify(recommendationService).refreshDailyRecommendedBooks();
    }

    @Test
    void retryRecommendedBooksRefreshSkipsWhenCacheIsFresh() {
        when(recommendationService.hasRecommendedBooks()).thenReturn(true);
        when(recommendationService.isRecommendedBooksStale()).thenReturn(false);

        scheduler.retryRecommendedBooksRefresh();

        verify(recommendationService, never()).refreshDailyRecommendedBooks();
    }

    @Test
    void updateDailyRecommendedBooksRunsAtSixAmInSeoul() throws NoSuchMethodException {
        Method method = BookRecommendationScheduler.class.getMethod("updateDailyRecommendedBooks");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled.cron()).isEqualTo("0 0 6 * * ?");
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
    }

    @Test
    void startupDailyAndRetryEntrypointsShareSingleFlightGuard() {
        when(recommendationService.hasRecommendedBooks()).thenReturn(false);
        doAnswer(invocation -> {
            scheduler.updateDailyRecommendedBooks();
            scheduler.retryRecommendedBooksRefresh();
            return bookList();
        }).when(recommendationService).refreshDailyRecommendedBooks();

        scheduler.initializeRecommendedBooks();

        verify(recommendationService).refreshDailyRecommendedBooks();
    }

    @Test
    void refreshFailureIsCapturedSwallowedAndNextRetryCanRunAgain() {
        RuntimeException failure = new RuntimeException("scheduler refresh failed");
        when(recommendationService.hasRecommendedBooks()).thenReturn(false);
        doThrow(failure)
                .doReturn(bookList())
                .when(recommendationService)
                .refreshDailyRecommendedBooks();

        assertDoesNotThrow(scheduler::retryRecommendedBooksRefresh);
        assertDoesNotThrow(scheduler::retryRecommendedBooksRefresh);

        verify(recommendationService, times(2)).refreshDailyRecommendedBooks();
        assertThat(captureClient.captured()).containsExactly(failure);
    }

    @Test
    void refreshFailureDoesNotLogRawThrowableOrCredentialBearingMessages() {
        RuntimeException failure = credentialBearingFailure();
        when(recommendationService.hasRecommendedBooks()).thenReturn(false);
        when(recommendationService.refreshDailyRecommendedBooks()).thenThrow(failure);
        ListAppender<ILoggingEvent> appender = attachSchedulerLogAppender();

        try {
            assertDoesNotThrow(scheduler::retryRecommendedBooksRefresh);

            assertThat(captureClient.captured()).containsExactly(failure);
            assertThat(appender.list)
                    .filteredOn(event -> event.getLevel().equals(Level.ERROR))
                    .singleElement()
                    .satisfies(event -> {
                        assertThat(event.getFormattedMessage()).doesNotContain(SECRET_TTB_KEY);
                        assertThat(event.getFormattedMessage()).doesNotContain(SECRET_REFRESH_TOKEN);
                        assertThat(event.getFormattedMessage()).doesNotContain(SECRET_AUTHORIZATION);
                        assertThat(event.getFormattedMessage()).doesNotContain(SECRET_COOKIE);
                        assertThat(event.getFormattedMessage()).doesNotContain("ttbkey=" + SECRET_TTB_KEY);
                        assertThat(event.getFormattedMessage()).contains("ttbkey=***");
                        assertThat(event.getFormattedMessage()).contains("Authorization: ***");
                        assertThat(event.getFormattedMessage()).contains("Cookie: ***");
                        assertThat(event.getFormattedMessage()).contains("refresh_token=***");
                        assertThat(event.getThrowableProxy()).isNull();
                    });
        } finally {
            detachSchedulerLogAppender(appender);
        }
    }

    private static BookResponseDTO.BookList bookList() {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of(BookResponseDTO.DetailInfo.builder()
                        .isbn("9791169213882")
                        .title("테스트 책")
                        .build()))
                .hasNext(false)
                .currentPage(null)
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
                new IllegalStateException(
                        "inner failure Cookie: " + SECRET_COOKIE
                )
        );
    }

    private static ListAppender<ILoggingEvent> attachSchedulerLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(BookRecommendationScheduler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private static void detachSchedulerLogAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(BookRecommendationScheduler.class);
        logger.detachAppender(appender);
        appender.stop();
    }
}
