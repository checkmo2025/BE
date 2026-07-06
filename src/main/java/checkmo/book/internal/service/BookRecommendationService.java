package checkmo.book.internal.service;

import checkmo.book.internal.RecommendationLogSanitizer;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.internal.util.DayOfWeekUtils;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.SentryCaptureClient;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookRecommendationService {

    private static final String REDIS_KEY = "book:recommendations:daily";
    private static final String REDIS_UPDATED_AT_KEY = "book:recommendations:daily:updated_at";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AladinApiService aladinApiService;
    private final AladinRecommendationRefreshClient recommendationRefreshClient;
    private final SentryCaptureClient sentryCaptureClient;

    public BookResponseDTO.BookList retrieveRecommendedBooks(Long memberId) {
        BookResponseDTO.BookList cachedBooks = retrieveUsableCachedBooks();

        if (cachedBooks != null && !isRecommendedBooksStale()) {
            return aladinApiService.applyLikedByMe(cachedBooks, memberId);
        }

        try {
            BookResponseDTO.BookList refreshedBooks = retrieveAndSaveRecommendedBooksFromAladin();
            return aladinApiService.applyLikedByMe(refreshedBooks, memberId);
        } catch (Exception e) {
            if (cachedBooks != null) {
                log.warn(
                        "추천 책 갱신 실패로 stale 캐시를 반환합니다. exceptionType={}, errorStatus={}, message={}, causeType={}, causeMessage={}",
                        e.getClass().getName(),
                        safeErrorStatus(e),
                        RecommendationLogSanitizer.sanitize(e.getMessage()),
                        safeCauseType(e),
                        safeCauseMessage(e)
                );
                sentryCaptureClient.captureException(e);
                return aladinApiService.applyLikedByMe(cachedBooks, memberId);
            }

            throw new BookException(BookErrorStatus.ALADIN_API_ERROR, e);
        }
    }

    public BookResponseDTO.BookList refreshDailyRecommendedBooks() {
        try {
            return retrieveAndSaveRecommendedBooksFromAladin();
        } catch (Exception e) {
            log.error(
                    "API에서 추천 책 가져오기 중 오류. exceptionType={}, errorStatus={}, message={}, causeType={}, causeMessage={}",
                    e.getClass().getName(),
                    safeErrorStatus(e),
                    RecommendationLogSanitizer.sanitize(e.getMessage()),
                    safeCauseType(e),
                    safeCauseMessage(e)
            );
            sentryCaptureClient.captureException(e);
            return createEmptyBookList();
        }
    }

    private BookResponseDTO.BookList retrieveUsableCachedBooks() {
        try {
            Object cachedData = redisTemplate.opsForValue().get(REDIS_KEY);

            if (cachedData instanceof BookResponseDTO.BookList bookList && isUsableBookList(bookList)) {
                return bookList;
            }
        } catch (Exception e) {
            log.error("Redis에서 추천 책 조회 중 오류 발생. API에서 데이터를 가져오기.", e);
        }

        return null;
    }

    private BookResponseDTO.BookList retrieveAndSaveRecommendedBooksFromAladin() {
        BookResponseDTO.BookList bookList = recommendationRefreshClient.retrieveRecommendedBooks();

        if (!isUsableBookList(bookList)) {
            log.error("알라딘 API에서 추천 책을 가져오지 못했습니다.");
            throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
        }

        int startIndex = DayOfWeekUtils.calculateStartIndexByDayOfWeek();
        var allBooks = bookList.getDetailInfoList();

        if (allBooks.size() < startIndex + 4) {
            log.error("추천 책 목록 부족, 전체: {}개, 필요: {}개", allBooks.size(), startIndex + 4);
            throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
        }

        List<DetailInfo> booksForToday = List.copyOf(allBooks.subList(startIndex, startIndex + 4));
        saveRecommendedBooks(booksForToday);

        return BookResponseDTO.BookList.builder()
                .detailInfoList(booksForToday)
                .hasNext(false)
                .currentPage(null)
                .build();
    }

    private boolean isUsableBookList(BookResponseDTO.BookList bookList) {
        return bookList != null
                && bookList.getDetailInfoList() != null
                && !bookList.getDetailInfoList().isEmpty();
    }

    private void saveRecommendedBooks(List<DetailInfo> books) {
        try {
            if (books == null || books.isEmpty()) {
                log.warn("추천 책 목록이 비어있습니다.");
                return;
            }

            BookResponseDTO.BookList bookList = BookResponseDTO.BookList.builder()
                    .detailInfoList(books)
                    .hasNext(false)
                    .currentPage(null)
                    .build();

            redisTemplate.opsForValue().set(REDIS_KEY, bookList);
            redisTemplate.opsForValue().set(REDIS_UPDATED_AT_KEY, LocalDate.now().toString());
        } catch (Exception e) {
            log.error("Redis에 추천 책 저장 중 오류 발생", e);
            sentryCaptureClient.captureException(e);
        }
    }

    public boolean hasRecommendedBooks() {
        try {
            return redisTemplate.hasKey(REDIS_KEY);
        } catch (Exception e) {
            log.error("Redis 키 확인 중 오류 발생", e);
            return false;
        }
    }

    public boolean isRecommendedBooksStale() {
        try {
            Object updatedAt = redisTemplate.opsForValue().get(REDIS_UPDATED_AT_KEY);

            if (updatedAt == null) {
                return true;
            }

            LocalDate cachedDate = LocalDate.parse(updatedAt.toString());
            return cachedDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            log.error("캐시 staleness 확인 중 오류 발생", e);
            return true;
        }
    }

    private BookResponseDTO.BookList createEmptyBookList() {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(Collections.emptyList())
                .hasNext(false)
                .currentPage(null)
                .build();
    }

    private String safeErrorStatus(Exception exception) {
        if (exception instanceof BookException bookException) {
            return bookException.getErrorReasonHttpStatus().getCode();
        }

        return null;
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
