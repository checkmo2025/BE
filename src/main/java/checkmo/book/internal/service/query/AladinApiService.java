package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.repository.BookLikedRepository;
import checkmo.book.internal.repository.BookRepository;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.CheckmoMetrics;
import checkmo.common.monitoring.SentryCaptureClient;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AladinApiService {

    private static final int LOG_BODY_MAX_LENGTH = 500;

    private final Yes24Client yes24Client;
    private final BookRepository bookRepository;

    private final Yes24Properties aladinProperties;
    private final BookLikedRepository bookLikedRepository;
    private final BookSearchCacheService bookSearchCacheService;
    private final AladinSearchClient aladinSearchClient;
    private final AladinSearchPrefetchService aladinSearchPrefetchService;
    private final SentryCaptureClient sentryCaptureClient;
    private final CheckmoMetrics checkmoMetrics;

    public BookResponseDTO.BookList retrieveSearchBooks(String keyword, int page, Long memberId) {
        if (keyword == null || keyword.isBlank()) {
            return emptySearchResult(page);
        }

        int maxResults = aladinProperties.getSearch().getMaxResults();

        var cachedBookList = bookSearchCacheService.retrieve(keyword, maxResults, page);
        if (cachedBookList.isPresent()) {
            return applyLikedByMe(cachedBookList.get(), memberId);
        }

        try {
            BookResponseDTO.BookList bookList = aladinSearchClient.fetchSearchBooks(keyword, page);
            bookSearchCacheService.save(keyword, maxResults, page, bookList);

            prefetchNextPageIfNeeded(keyword, page, bookList);
            return applyLikedByMe(bookList, memberId);

        } catch (Exception e) {
            logAladinSearchFailure(e);
            var fallbackBookList = bookSearchCacheService.retrieve(keyword, maxResults, page);
            if (fallbackBookList.isPresent()) {
                sentryCaptureClient.captureException(e);
                return applyLikedByMe(fallbackBookList.get(), memberId);
            }
            throw new BookException(BookErrorStatus.ALADIN_API_ERROR, e);
        }
    }

    private BookResponseDTO.BookList emptySearchResult(int page) {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of())
                .hasNext(false)
                .currentPage(page)
                .totalResults(0)
                .build();
    }

    private void prefetchNextPageIfNeeded(String keyword, int page, BookResponseDTO.BookList bookList) {
        if (bookList == null || !bookList.isHasNext()) {
            return;
        }

        try {
            aladinSearchPrefetchService.prefetchNextPage(keyword, page);
        } catch (Exception e) {
            log.warn("예스24 검색 다음 페이지 prefetch 요청 실패. page={}", page + 1, e);
            sentryCaptureClient.captureException(e);
        }
    }

    public DetailInfo retrieveBookDetailInfo(String isbn) {
        Timer.Sample sample = checkmoMetrics.startTimer();
        String result = "success";
        Exception failure = null;
        try {
            return yes24Client.retrieveBookDetail(isbn);
        } catch (BookException e) {
            failure = e;
            result = checkmoMetrics.classifyAladinResult(e);
            if (isbn != null) {
                var stored = bookRepository.findById(isbn);
                if (stored.isPresent()) {
                    var book = stored.get();
                    sentryCaptureClient.captureException(e);
                    return BookResponseDTO.DetailInfo.builder()
                            .isbn(book.getId()).title(book.getTitle()).author(book.getAuthor())
                            .publisher(book.getPublisher()).imgUrl(book.getImgUrl())
                            .description(book.getDescription()).link(null).likedByMe(false).build();
                }
            }
            throw e;
        } finally {
            checkmoMetrics.recordAladinClientResult(sample, "detail", result, failure);
        }
    }

    public BookResponseDTO.BookList retrieveRecommendedBooks() {
        Timer.Sample sample = checkmoMetrics.startTimer();
        try {
            var response = yes24Client.retrieveRecommendedBooks();
            checkmoMetrics.recordAladinClientResult(sample, "recommendation", "success", null);
            return response;
        } catch (RuntimeException e) {
            checkmoMetrics.recordAladinClientResult(sample, "recommendation", checkmoMetrics.classifyAladinResult(e), e);
            throw e;
        }
    }

    private void logAladinSearchFailure(Exception exception) {
        log.error(
                "Yes24 API request failed. operation={}, exceptionType={}, message={}",
                "retrieveSearchBooks",
                exception.getClass().getName(),
                sanitize(exception.getMessage())
        );

        Throwable cause = exception.getCause();
        if (cause != null) {
            log.error(
                    "Yes24 API failure cause. operation={}, causeType={}, causeMessage={}",
                    "retrieveSearchBooks",
                    cause.getClass().getName(),
                    sanitize(cause.getMessage())
            );
        }

        if (exception instanceof RestClientResponseException responseException) {
            log.error(
                    "Yes24 API response failure. operation={}, statusCode={}, responseBody={}",
                    "retrieveSearchBooks",
                    responseException.getStatusCode(),
                    trimForLog(sanitize(responseException.getResponseBodyAsString()))
            );
        }
    }

    private String maskTtbKey(String value) {
        return sanitize(value);
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("(?i)((?:ttbkey|query|keyword|authorization|cookie|jwt|accessToken|refreshToken|password|verification[-_]?code)=)[^&\\s]+", "$1***");
    }

    private String trimForLog(String value) {
        if (value == null || value.length() <= LOG_BODY_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, LOG_BODY_MAX_LENGTH) + "...";
    }

    public BookResponseDTO.BookList applyLikedByMe(BookResponseDTO.BookList bookList, Long memberId) {
        if (bookList == null || bookList.getDetailInfoList() == null || bookList.getDetailInfoList().isEmpty()) {
            return bookList;
        }

        List<String> bookIds = bookList.getDetailInfoList().stream()
                .map(DetailInfo::getIsbn)
                .toList();
        Set<String> likedBookIds = bookLikedRepository.findLikedBookIdSet(memberId, bookIds);

        List<DetailInfo> updatedDetails = bookList.getDetailInfoList().stream()
                .map(detail -> DetailInfo.builder()
                        .isbn(detail.getIsbn())
                        .title(detail.getTitle())
                        .author(detail.getAuthor())
                        .imgUrl(detail.getImgUrl())
                        .publisher(detail.getPublisher())
                        .description(detail.getDescription())
                        .link(detail.getLink())
                        .likedByMe(likedBookIds.contains(detail.getIsbn()))
                        .build())
                .toList();

        return BookResponseDTO.BookList.builder()
                .detailInfoList(updatedDetails)
                .hasNext(bookList.isHasNext())
                .currentPage(bookList.getCurrentPage())
                .totalResults(bookList.getTotalResults())
                .build();
    }
}
