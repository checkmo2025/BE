package checkmo.book.internal.scheduler;

import checkmo.book.internal.service.BookRecommendationService;
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

    @Scheduled(cron = "0 0 0 * * ?")
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
            recommendationService.refreshDailyRecommendedBooks();
            log.info("추천 책 갱신 완료");
        } catch (Exception e) {
            log.error("추천 책 갱신 중 오류 발생", e);
        }
    }
}