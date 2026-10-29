package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.converter.BookConverter;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.Yes24ApiResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class Yes24Client {

    private final RestTemplate restTemplate;
    private final Yes24Properties properties;
    private final ObjectMapper objectMapper;
    private final Yes24RateLimiter rateLimiter;

    public BookResponseDTO.BookList retrieveSearchBooks(String keyword, int page) {
        var uri = endpoint("/goods/itemList")
                .queryParam("query", "{query}").queryParam("category", "BOOK")
                .queryParam("sort", "DEFAULT").queryParam("page", page)
                .queryParam("pageSize", properties.getSearch().getMaxResults());
        return BookConverter.toYes24BookList(request(uri, "search", keyword));
    }

    public BookResponseDTO.DetailInfo retrieveBookDetail(String isbn) {
        if (isbn == null || !isbn.matches("97[89][0-9]{10}")) {
            throw new BookException(BookErrorStatus.BOOK_NOT_FOUND);
        }
        var response = request(endpoint("/goods/itemDetail")
                .queryParam("searchType", "ISBN13").queryParam("query", "{query}"), "detail", isbn);
        return BookConverter.toYes24BookDetail(response, isbn);
    }

    public BookResponseDTO.BookList retrieveRecommendedBooks() {
        return BookConverter.toYes24BookList(request(endpoint("/category/bestseller")
                .queryParam("categoryId", "001").queryParam("page", 1)
                .queryParam("pageSize", 28), "recommendation"));
    }

    private UriComponentsBuilder endpoint(String path) {
        return UriComponentsBuilder.fromUriString(properties.getBaseUrl() + path).queryParam("detail", "N");
    }

    private Yes24ApiResponseDTO.Response request(UriComponentsBuilder uri, String operation, Object... values) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BookException(BookErrorStatus.YES24_API_ERROR);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", properties.getApiKey());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        rateLimiter.acquire();
        try {
            return restTemplate.exchange(uri.encode().buildAndExpand(values).toUri(), HttpMethod.GET,
                    new HttpEntity<>(headers), Yes24ApiResponseDTO.Response.class).getBody();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 429 || e.getStatusCode().value() == 503) {
                rateLimiter.defer(e.getResponseHeaders() == null ? null
                        : e.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER));
            }
            String code = errorCode(e);
            if (e.getStatusCode().value() == 404) {
                if ("search".equals(operation) && "SEARCH_001".equals(code)) {
                    return new Yes24ApiResponseDTO.Response(true, null,
                            new Yes24ApiResponseDTO.Data(List.of(), 1, properties.getSearch().getMaxResults(), 0));
                }
                if ("detail".equals(operation) && ("GOODS_001".equals(code) || "GOODS_002".equals(code))) {
                    throw new BookException(BookErrorStatus.BOOK_NOT_FOUND);
                }
            }
            // 공급자가 키/검색어를 본문·헤더에 반사하더라도 예외 체인으로 전달하지 않는다.
            HttpHeaders safeHeaders = new HttpHeaders();
            if (e.getResponseHeaders() != null) {
                String retryAfter = e.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER);
                if (retryAfter != null && retryAfter.matches("[0-9]{1,9}")) {
                    safeHeaders.set(HttpHeaders.RETRY_AFTER, retryAfter);
                }
            }
            throw new BookException(BookErrorStatus.YES24_API_ERROR,
                    new RestClientResponseException("Yes24 " + operation + " request failed",
                            e.getStatusCode(), "", safeHeaders, null, null));
        } catch (RestClientException e) {
            throw new BookException(BookErrorStatus.YES24_API_ERROR,
                    new RestClientException("Yes24 " + operation + " request failed"));
        }
    }

    private String errorCode(RestClientResponseException failure) {
        try {
            return objectMapper.readTree(failure.getResponseBodyAsString()).path("errorCode").asText();
        } catch (Exception ignored) {
            return "";
        }
    }
}
