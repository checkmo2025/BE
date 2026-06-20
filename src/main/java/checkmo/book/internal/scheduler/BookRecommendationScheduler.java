package checkmo.book.internal.scheduler;

import checkmo.book.internal.RecommendationLogSanitizer;
import checkmo.book.internal.service.BookRecommendationService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.SentryCaptureClient;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookRecommendationScheduler {

    private final BookRecommendationService recommendationService;
    private final SentryCaptureClient sentryCaptureClient;
    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void updateDailyRecommendedBooks() {
        log.info("일일 추천 책 갱신 시작");
        guardedRefreshRecommendedBooks();
    }

    @Scheduled(fixedDelayString = "${aladin.api.recommendation.refresh.background.fixed-delay}")
    public void retryRecommendedBooksRefresh() {
        if (recommendationService.hasRecommendedBooks() && !recommendationService.isRecommendedBooksStale()) {
            log.debug("추천 책 캐시가 최신 상태라 background retry를 건너뜁니다.");
            return;
        }

        log.info("추천 책 캐시가 없거나 오래되어 background retry 갱신 시작");
        guardedRefreshRecommendedBooks();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRecommendedBooks() {
        if (!recommendationService.hasRecommendedBooks() || recommendationService.isRecommendedBooksStale()) {
            log.info("서버 시작 시 Redis에 추천 책 데이터가 없거나 오래됨. 알라딘에 요청 시작");
            guardedRefreshRecommendedBooks();
            return;
        }

        log.info("서버 시작 시 Redis에 최신 추천 책 데이터가 존재");
    }

    private void guardedRefreshRecommendedBooks() {
        if (!refreshInProgress.compareAndSet(false, true)) {
            log.info("추천 책 갱신이 이미 진행 중이라 이번 요청을 건너뜁니다.");
            return;
        }

        try {
            BookResponseDTO.BookList refreshedBooks = recommendationService.refreshDailyRecommendedBooks();
            if (isEmpty(refreshedBooks)) {
                log.warn("추천 책 갱신 결과가 비어있어 기존 캐시 상태를 유지합니다.");
                return;
            }

            log.info("추천 책 갱신 완료");
        } catch (Exception e) {
            log.error(
                    "추천 책 갱신 중 예기치 못한 오류 발생 exceptionType={} exceptionMessage={} causeType={} causeMessage={}",
                    e.getClass().getName(),
                    RecommendationLogSanitizer.sanitize(e.getMessage()),
                    safeCauseType(e),
                    safeCauseMessage(e)
            );
            sentryCaptureClient.captureException(e);
        } finally {
            refreshInProgress.set(false);
        }
    }

    private boolean isEmpty(BookResponseDTO.BookList bookList) {
        if (bookList == null) {
            return true;
        }

        List<BookResponseDTO.DetailInfo> books = bookList.getDetailInfoList();
        return books == null || books.isEmpty();
    }

    private String safeCauseType(Exception exception) {
        Throwable cause = exception.getCause();
        if (cause == null) {
            return null;
        }

        return cause.getClass().getName();
    }

    private String safeCauseMessage(Exception exception) {
        Throwable cause = exception.getCause();
        if (cause == null) {
            return null;
        }

        return RecommendationLogSanitizer.sanitize(cause.getMessage());
    }
}
