package checkmo.book.internal.scheduler;

import checkmo.book.internal.service.BookRecommendationService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.SentryCaptureClient;
import java.util.List;
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

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void updateDailyRecommendedBooks() {
        log.info("일일 추천 책 갱신 시작");
        retrieveAndSaveRecommendedBooks();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRecommendedBooks() {
        if (!recommendationService.hasRecommendedBooks() || recommendationService.isRecommendedBooksStale()) {
            log.info("서버 시작 시 Redis에 추천 책 데이터가 없거나 오래됨. 알라딘에 요청 시작");
            retrieveAndSaveRecommendedBooks();
            return;
        }

        log.info("서버 시작 시 Redis에 최신 추천 책 데이터가 존재");
    }

    private void retrieveAndSaveRecommendedBooks() {
        try {
            BookResponseDTO.BookList refreshedBooks = recommendationService.refreshDailyRecommendedBooks();
            if (isEmpty(refreshedBooks)) {
                log.warn("추천 책 갱신 결과가 비어있어 기존 캐시 상태를 유지합니다.");
                return;
            }

            log.info("추천 책 갱신 완료");
        } catch (Exception e) {
            log.error("추천 책 갱신 중 예기치 못한 오류 발생", e);
            sentryCaptureClient.captureException(e);
        }
    }

    private boolean isEmpty(BookResponseDTO.BookList bookList) {
        if (bookList == null) {
            return true;
        }

        List<BookResponseDTO.DetailInfo> books = bookList.getDetailInfoList();
        return books == null || books.isEmpty();
    }
}
