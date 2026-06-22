package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.web.dto.AladinApiResponseDTO;
import checkmo.book.web.dto.BookResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AladinSearchClient {

    private final RestTemplate restTemplate;
    private final AladinProperties aladinProperties;

    public BookResponseDTO.BookList fetchSearchBooks(String keyword, int page) {
        long totalStartNanos = System.nanoTime();
        AladinApiResponseDTO.BookList response;
        try {
            long httpStartNanos = System.nanoTime();
            response = restTemplate.getForObject(
                    buildHttpUrl(keyword, page),
                    AladinApiResponseDTO.BookList.class
            );
            log.info("book-search-timing stage=aladinHttp traceId={} page={} rawItemCount={} rawTotalResults={} elapsedMs={}",
                    traceId(), page, rawItemCount(response), rawTotalResults(response), elapsedMillis(httpStartNanos));
        } catch (RestClientException e) {
            log.info("book-search-timing stage=aladinHttpFailure traceId={} page={} elapsedMs={} exceptionType={}",
                    traceId(), page, elapsedMillis(totalStartNanos), e.getClass().getName());
            throw new IllegalStateException(sanitizedFailureMessage(e));
        }

        long convertStartNanos = System.nanoTime();
        BookResponseDTO.BookList bookList = BookConverter.toBookList(response, page);
        log.info("book-search-timing stage=aladinConvert traceId={} page={} itemCount={} hasNext={} totalResults={} elapsedMs={}",
                traceId(), page, detailCount(bookList), bookList.isHasNext(), bookList.getTotalResults(),
                elapsedMillis(convertStartNanos));
        log.info("book-search-timing stage=aladinFetchTotal traceId={} page={} elapsedMs={}",
                traceId(), page, elapsedMillis(totalStartNanos));
        return bookList;
    }

    private String sanitizedFailureMessage(RestClientException exception) {
        if (exception instanceof RestClientResponseException responseException) {
            return "Aladin API request failed for search books. exceptionType=%s, statusCode=%s".formatted(
                    exception.getClass().getName(),
                    responseException.getStatusCode()
            );
        }
        return "Aladin API request failed for search books. exceptionType=%s".formatted(
                exception.getClass().getName()
        );
    }

    private String buildHttpUrl(String keyword, int page) {
        return UriComponentsBuilder
                .fromUriString(aladinProperties.getUrl().getBase() + aladinProperties.getUrl().getItemSearch())
                .queryParam("ttbkey", aladinProperties.getAuth().getTtbKey())
                .queryParam("Query", keyword)
                .queryParam("QueryType", aladinProperties.getSearch().getSearchQueryType())
                .queryParam("MaxResults", aladinProperties.getSearch().getMaxResults())
                .queryParam("start", page)
                .queryParam("output", aladinProperties.getSearch().getOutput())
                .queryParam("Version", aladinProperties.getAuth().getVersion())
                .build()
                .toUriString();
    }

    private String traceId() {
        return MDC.get(AladinApiService.BOOK_SEARCH_TRACE_ID);
    }

    private int rawItemCount(AladinApiResponseDTO.BookList response) {
        if (response == null || response.getItems() == null) {
            return 0;
        }
        return response.getItems().size();
    }

    private int rawTotalResults(AladinApiResponseDTO.BookList response) {
        if (response == null) {
            return 0;
        }
        return response.getTotalResults();
    }

    private int detailCount(BookResponseDTO.BookList bookList) {
        if (bookList == null || bookList.getDetailInfoList() == null) {
            return 0;
        }
        return bookList.getDetailInfoList().size();
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
