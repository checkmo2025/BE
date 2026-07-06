package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.repository.BookLikedRepository;
import checkmo.book.web.dto.AladinApiResponseDTO;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.common.monitoring.SentryCaptureClient;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AladinApiService {

    private static final int RECOMMENDED_MAX_RESULTS = 28;
    private static final int LOG_BODY_MAX_LENGTH = 500;

    private final RestTemplate restTemplate;

    private final AladinProperties aladinProperties;
    private final BookLikedRepository bookLikedRepository;
    private final BookSearchCacheService bookSearchCacheService;
    private final AladinSearchClient aladinSearchClient;
    private final AladinSearchPrefetchService aladinSearchPrefetchService;
    private final SentryCaptureClient sentryCaptureClient;

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
            log.warn("알라딘 검색 다음 페이지 prefetch 요청 실패. page={}", page + 1, e);
            sentryCaptureClient.captureException(e);
        }
    }

    public DetailInfo retrieveBookDetailInfo(String isbn) {
        String url = buildHttpUrl(isbn);
        try {
            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.BookList.class
            );

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                throw new BookException(BookErrorStatus.BOOK_NOT_FOUND);
            }

            return BookConverter.toBookInfoDetail(response);

        } catch (BookException e) {
            logAladinFailure("retrieveBookDetailInfo", url, e);
            throw new BookException(BookErrorStatus.BOOK_NOT_FOUND, e);
        } catch (Exception e) {
            logAladinFailure("retrieveBookDetailInfo", url, e);
            throw new BookException(BookErrorStatus.ALADIN_API_ERROR, e);
        }
    }

    public BookResponseDTO.BookList retrieveRecommendedBooks() {
        String url = buildHttpUrl();
        try {
            var response = restTemplate.getForObject(
                    url,
                    AladinApiResponseDTO.BookList.class
            );

            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                throw new BookException(BookErrorStatus.ALADIN_API_ERROR);
            }

            return BookConverter.toBookList(response, 1);

        } catch (Exception e) {
            logAladinFailure("retrieveRecommendedBooks", url, e);
            throw new BookException(BookErrorStatus.ALADIN_API_ERROR, e);
        }
    }

    private String buildHttpUrl() {
        return UriComponentsBuilder
                .fromUriString(aladinProperties.getUrl().getBase() + aladinProperties.getUrl().getItemList())
                .queryParam("ttbkey", aladinProperties.getAuth().getTtbKey())
                .queryParam("QueryType", aladinProperties.getSearch().getRecommendQueryType())
                .queryParam("MaxResults", RECOMMENDED_MAX_RESULTS)
                .queryParam("SearchTarget", aladinProperties.getSearch().getSearchTarget())
                .queryParam("output", aladinProperties.getSearch().getOutput())
                .queryParam("Version", aladinProperties.getAuth().getVersion())
                .build()
                .toUriString();
    }

    private String buildHttpUrl(String isbn) {
        return UriComponentsBuilder
                .fromUriString(aladinProperties.getUrl().getBase() + aladinProperties.getUrl().getItemLookup())
                .queryParam("ttbkey", aladinProperties.getAuth().getTtbKey())
                .queryParam("itemIdType", aladinProperties.getSearch().getItemIdType())
                .queryParam("ItemId", isbn)
                .queryParam("output", aladinProperties.getSearch().getOutput())
                .queryParam("Version", aladinProperties.getAuth().getVersion())
                .build()
                .toUriString();
    }

    private void logAladinFailure(String operation, String url, Exception exception) {
        log.error(
                "Aladin API request failed. operation={}, url={}, exceptionType={}, message={}",
                operation,
                maskTtbKey(url),
                exception.getClass().getName(),
                sanitize(exception.getMessage())
        );

        Throwable cause = exception.getCause();
        if (cause != null) {
            log.error(
                    "Aladin API failure cause. operation={}, causeType={}, causeMessage={}",
                    operation,
                    cause.getClass().getName(),
                    sanitize(cause.getMessage())
            );
        }

        if (exception instanceof RestClientResponseException responseException) {
            log.error(
                    "Aladin API response failure. operation={}, statusCode={}, responseBody={}",
                    operation,
                    responseException.getStatusCode(),
                    trimForLog(sanitize(responseException.getResponseBodyAsString()))
            );
        }
    }

    private void logAladinSearchFailure(Exception exception) {
        log.error(
                "Aladin API request failed. operation={}, exceptionType={}, message={}",
                "retrieveSearchBooks",
                exception.getClass().getName(),
                sanitize(exception.getMessage())
        );

        Throwable cause = exception.getCause();
        if (cause != null) {
            log.error(
                    "Aladin API failure cause. operation={}, causeType={}, causeMessage={}",
                    "retrieveSearchBooks",
                    cause.getClass().getName(),
                    sanitize(cause.getMessage())
            );
        }

        if (exception instanceof RestClientResponseException responseException) {
            log.error(
                    "Aladin API response failure. operation={}, statusCode={}, responseBody={}",
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
