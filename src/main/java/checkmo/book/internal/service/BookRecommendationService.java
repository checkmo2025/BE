package checkmo.book.internal.service;

import checkmo.book.internal.RecommendationLogSanitizer;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.internal.util.DayOfWeekUtils;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.CheckmoMetrics;
import checkmo.common.monitoring.SentryCaptureClient;
import io.micrometer.core.instrument.Timer;
import java.time.LocalDate;
import java.time.ZoneId;
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

    private static final String REDIS_KEY = RecommendationRefreshGuard.DATA_KEY;
    private static final String REDIS_UPDATED_AT_KEY = RecommendationRefreshGuard.DATE_KEY;

    private final RedisTemplate<String, Object> redisTemplate;
    private final AladinApiService aladinApiService;
    private final AladinRecommendationRefreshClient recommendationRefreshClient;
    private final SentryCaptureClient sentryCaptureClient;
    private final CheckmoMetrics checkmoMetrics;
    private final RecommendationRefreshGuard refreshGuard;

    public BookResponseDTO.BookList retrieveRecommendedBooks(Long memberId) {
        CacheLookup cacheLookup = retrieveUsableCachedBooks();
        BookResponseDTO.BookList cachedBooks = cacheLookup.bookList();
        checkmoMetrics.incrementRecommendationCacheRequest(cacheLookup.result());

        if (cachedBooks != null && "fresh".equals(cacheLookup.result())) {
            return aladinApiService.applyLikedByMe(cachedBooks, memberId);
        }

        try {
            BookResponseDTO.BookList refreshedBooks = retrieveAndSaveRecommendedBooksFromAladin("user_request");
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
            return retrieveAndSaveRecommendedBooksFromAladin("background");
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

    private CacheLookup retrieveUsableCachedBooks() {
        try {
            var pair = redisTemplate.opsForValue().multiGet(List.of(REDIS_KEY, REDIS_UPDATED_AT_KEY));
            if (pair != null && pair.size() == 2 && pair.get(0) instanceof BookResponseDTO.BookList bookList
                    && isUsableBookList(bookList)) {
                boolean fresh = today().toString().equals(pair.get(1));
                return new CacheLookup(bookList, fresh ? "fresh" : "stale");
            }
            // 전환 첫날에만 구 공급자 캐시를 장애 fallback으로 사용한다. 최신으로 취급하지 않는다.
            Object legacy = redisTemplate.opsForValue().get("book:recommendations:daily");
            if (legacy instanceof BookResponseDTO.BookList bookList && isUsableBookList(bookList)) {
                return new CacheLookup(bookList, "stale");
            }
        } catch (Exception e) {
            log.error("Redis에서 추천 책 조회 중 오류 발생. API에서 데이터를 가져오기.", e);
            return new CacheLookup(null, "redis_error");
        }

        return new CacheLookup(null, "miss");
    }

    private BookResponseDTO.BookList retrieveAndSaveRecommendedBooksFromAladin(String source) {
        String token = refreshGuard.acquire();
        try {
            var cached = retrieveUsableCachedBooks();
            if ("fresh".equals(cached.result())) return cached.bookList();
            return refreshRecommendedBooks(source, token);
        } finally {
            refreshGuard.release(token);
        }
    }

    private BookResponseDTO.BookList refreshRecommendedBooks(String source, String token) {
        Timer.Sample sample = checkmoMetrics.startTimer();
        String result = "success";
        try {
            BookResponseDTO.BookList bookList = recommendationRefreshClient.retrieveRecommendedBooks();

            if (!isUsableBookList(bookList)) {
                result = "invalid_response";
                log.error("알라딘 API에서 추천 책을 가져오지 못했습니다.");
                throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
            }

            LocalDate day = today();
            int startIndex = DayOfWeekUtils.calculateStartIndexByDayOfWeek(day);
            var allBooks = bookList.getDetailInfoList();
            checkmoMetrics.recordRecommendationBooksCount("aladin", allBooks.size());

            if (allBooks.size() < startIndex + 4) {
                result = "invalid_response";
                log.error("추천 책 목록 부족, 전체: {}개, 필요: {}개", allBooks.size(), startIndex + 4);
                throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
            }

            List<DetailInfo> booksForToday = List.copyOf(allBooks.subList(startIndex, startIndex + 4));
            checkmoMetrics.recordRecommendationBooksCount("returned", booksForToday.size());
            saveRecommendedBooks(token, booksForToday, day);

            return BookResponseDTO.BookList.builder()
                    .detailInfoList(booksForToday)
                    .hasNext(false)
                    .currentPage(null)
                    .build();
        } catch (RuntimeException e) {
            if ("success".equals(result)) {
                result = checkmoMetrics.classifyAladinResult(e);
            }
            throw e;
        } finally {
            checkmoMetrics.recordAladinRecommendationRefresh(sample, result, source);
        }
    }

    private boolean isUsableBookList(BookResponseDTO.BookList bookList) {
        return bookList != null
                && bookList.getDetailInfoList() != null
                && !bookList.getDetailInfoList().isEmpty();
    }

    private void saveRecommendedBooks(String token, List<DetailInfo> books, LocalDate day) {
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

            refreshGuard.publish(token, bookList, day.toString());
        } catch (Exception e) {
            log.error("Redis에 추천 책 저장 중 오류 발생", e);
            throw e;
        }
    }

    public boolean hasRecommendedBooks() {
        return retrieveUsableCachedBooks().bookList() != null;
    }

    public boolean isRecommendedBooksStale() {
        return !"fresh".equals(retrieveUsableCachedBooks().result());
    }

    private LocalDate today() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"));
    }

    private BookResponseDTO.BookList createEmptyBookList() {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(Collections.emptyList())
                .hasNext(false)
                .currentPage(null)
                .build();
    }

    private record CacheLookup(BookResponseDTO.BookList bookList, String result) {
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
