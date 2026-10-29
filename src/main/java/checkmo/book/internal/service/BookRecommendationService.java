package checkmo.book.internal.service;

import checkmo.book.internal.RecommendationLogSanitizer;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.service.query.Yes24ApiService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.CheckmoMetrics;
import checkmo.common.monitoring.SentryCaptureClient;
import io.micrometer.core.instrument.Timer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookRecommendationService {

    static final int MAX_CANDIDATES_PER_CATEGORY = 5;
    private static final List<String> CATEGORY_IDS = List.of("001001046", "001001019", "001001022", "001001007");

    private static final String REDIS_KEY = RecommendationRefreshGuard.DATA_KEY;
    private static final String REDIS_UPDATED_AT_KEY = RecommendationRefreshGuard.DATE_KEY;

    private final RedisTemplate<String, Object> redisTemplate;
    private final Yes24ApiService yes24ApiService;
    private final Yes24RecommendationRefreshClient recommendationRefreshClient;
    private final SentryCaptureClient sentryCaptureClient;
    private final CheckmoMetrics checkmoMetrics;
    private final RecommendationRefreshGuard refreshGuard;

    public BookResponseDTO.BookList retrieveRecommendedBooks(Long memberId) {
        CacheLookup cacheLookup = retrieveUsableCachedBooks();
        BookResponseDTO.BookList cachedBooks = cacheLookup.bookList();
        checkmoMetrics.incrementRecommendationCacheRequest(cacheLookup.result());

        if (cachedBooks != null && "fresh".equals(cacheLookup.result())) {
            return yes24ApiService.applyLikedByMe(cachedBooks, memberId);
        }

        try {
            BookResponseDTO.BookList refreshedBooks = retrieveAndSaveRecommendedBooksFromYes24("user_request");
            return yes24ApiService.applyLikedByMe(refreshedBooks, memberId);
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
                return yes24ApiService.applyLikedByMe(cachedBooks, memberId);
            }

            throw new BookException(BookErrorStatus.YES24_API_ERROR, e);
        }
    }

    public BookResponseDTO.BookList refreshDailyRecommendedBooks() {
        try {
            return retrieveAndSaveRecommendedBooksFromYes24("background");
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
        } catch (Exception e) {
            log.error("Redis에서 추천 책 조회 중 오류 발생. API에서 데이터를 가져오기.", e);
            return new CacheLookup(null, "redis_error");
        }

        return new CacheLookup(null, "miss");
    }

    private BookResponseDTO.BookList retrieveAndSaveRecommendedBooksFromYes24(String source) {
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
            LocalDate day = today();
            LocalDate referenceDate = day.withDayOfMonth(1).minusMonths(1);
            List<DetailInfo> booksForToday = new ArrayList<>();
            Set<String> selectedIsbns = new HashSet<>();
            for (String categoryId : CATEGORY_IDS) {
                DetailInfo book = retrieveCategoryRecommendation(categoryId, referenceDate, day.getDayOfMonth(), selectedIsbns);
                booksForToday.add(book);
                selectedIsbns.add(book.getIsbn());
            }
            booksForToday = List.copyOf(booksForToday);
            checkmoMetrics.recordRecommendationBooksCount("returned", booksForToday.size());
            saveRecommendedBooks(token, booksForToday, day);

            return BookResponseDTO.BookList.builder()
                    .detailInfoList(booksForToday)
                    .hasNext(false)
                    .currentPage(null)
                    .build();
        } catch (RuntimeException e) {
            if ("success".equals(result)) {
                result = checkmoMetrics.classifyYes24Result(e);
            }
            throw e;
        } finally {
            checkmoMetrics.recordYes24RecommendationRefresh(sample, result, source);
        }
    }

    private DetailInfo retrieveCategoryRecommendation(String categoryId, LocalDate date, int rank, Set<String> selectedIsbns) {
        Set<Integer> visitedPages = new HashSet<>();
        int page = rank;
        for (int attempt = 0; attempt < MAX_CANDIDATES_PER_CATEGORY && visitedPages.add(page); attempt++) {
            var candidates = recommendationRefreshClient.retrieveRecommendedBooks(categoryId, date, page);
            if (candidates == null || candidates.getDetailInfoList() == null) {
                throw new BookException(BookErrorStatus.YES24_API_ERROR);
            }
            var books = candidates.getDetailInfoList();
            checkmoMetrics.recordRecommendationBooksCount("yes24", books.size());
            if (books.size() == 1 && !selectedIsbns.contains(books.getFirst().getIsbn())) {
                return books.getFirst();
            }
            page = candidates.isHasNext() ? page + 1 : 1;
        }
        throw new BookException(BookErrorStatus.YES24_API_ERROR);
    }

    private boolean isUsableBookList(BookResponseDTO.BookList bookList) {
        return bookList != null
                && bookList.getDetailInfoList() != null
                && bookList.getDetailInfoList().size() == CATEGORY_IDS.size()
                && bookList.getDetailInfoList().stream().allMatch(book -> book != null && book.getIsbn() != null)
                && bookList.getDetailInfoList().stream().map(DetailInfo::getIsbn).distinct().count() == CATEGORY_IDS.size();
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

    LocalDate today() {
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
