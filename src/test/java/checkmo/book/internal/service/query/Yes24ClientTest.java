package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

class Yes24ClientTest {
    private static final String ISBN = "9788936434595";
    private Yes24Client client;
    private MockRestServiceServer server;
    private Yes24Properties properties;

    @BeforeEach
    void setUp() {
        var restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        properties = new Yes24Properties();
        properties.setApiKey("test-only-secret");
        client = new Yes24Client(restTemplate, properties, new ObjectMapper());
    }

    @Test
    void searchesBooksWithDefaultSortAndEncodesKeywordAsData() {
        server.expect(requestTo("https://apis.yes24.com/v1/goods/itemList?detail=N&query=C%2B%2B%20%26%20%EC%B1%85&category=BOOK&sort=DEFAULT&page=2&pageSize=10"))
                .andExpect(method(HttpMethod.GET)).andExpect(header("X-Api-Key", "test-only-secret"))
                .andExpect(header("Accept", "application/json"))
                .andRespond(withSuccess(payload("{\"isbn13\":\"" + ISBN + "\"}", 2, 10, 30), MediaType.APPLICATION_JSON));
        var result = client.retrieveSearchBooks("C++ & 책", 2);
        assertThat(result.getDetailInfoList()).singleElement().extracting("isbn").isEqualTo(ISBN);
        assertThat(result.isHasNext()).isTrue();
        server.verify();
    }

    @Test
    void retrievesTwentyEightRecommendationsInOneRequest() {
        String items = IntStream.range(0, 28).mapToObj(i -> "{\"isbn13\":\"" + ISBN + "\"}")
                .collect(Collectors.joining(","));
        server.expect(requestTo("https://apis.yes24.com/v1/category/bestseller?detail=N&categoryId=001&page=1&pageSize=28"))
                .andRespond(withSuccess(payload(items, 1, 28, 1000), MediaType.APPLICATION_JSON));
        assertThat(client.retrieveRecommendedBooks().getDetailInfoList()).hasSize(28);
        server.verify();
    }

    @Test
    void onlyExactIsbnFromDetailResponseIsSelected() {
        server.expect(requestTo("https://apis.yes24.com/v1/goods/itemDetail?detail=N&searchType=ISBN13&query=" + ISBN))
                .andRespond(withSuccess(payload("{\"isbn13\":\"9788966260959\"},{\"isbn13\":\"" + ISBN + "\"}", 1, 20, 2), MediaType.APPLICATION_JSON));
        assertThat(client.retrieveBookDetail(ISBN).getIsbn()).isEqualTo(ISBN);
        server.verify();
    }

    @Test
    void searchNotFoundKeepsSuccessfulEmptyContract() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND)
                .body("{\"success\":false,\"errorCode\":\"SEARCH_001\"}").contentType(MediaType.APPLICATION_JSON));
        var result = client.retrieveSearchBooks("없는책", 1);
        assertThat(result.getDetailInfoList()).isEmpty();
        assertThat(result.getCurrentPage()).isNull();
        assertThat(result.getTotalResults()).isZero();
        assertThat(result.isHasNext()).isFalse();
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({"404,GOODS_001,BOOK_NOT_FOUND", "404,GOODS_002,BOOK_NOT_FOUND",
            "404,SEARCH_002,ALADIN_API_ERROR", "401,AUTH_002,ALADIN_API_ERROR",
            "429,RATE_LIMIT,ALADIN_API_ERROR", "503,UPSTREAM_UNAVAILABLE,ALADIN_API_ERROR"})
    void distinguishesMissingDetailFromProviderFailure(int status, String code, BookErrorStatus expected) {
        server.expect(anything()).andRespond(withStatus(HttpStatus.valueOf(status))
                .body("{\"errorCode\":\"" + code + "\",\"message\":\"test-only-secret\"}")
                .contentType(MediaType.APPLICATION_JSON).header("X-Api-Key", "test-only-secret"));
        assertThatThrownBy(() -> client.retrieveBookDetail(ISBN)).isInstanceOfSatisfying(BookException.class, e -> {
            assertThat(e.getErrorCode()).isEqualTo(expected);
            assertThat(stackTrace(e)).doesNotContain("test-only-secret");
            if (e.getCause() instanceof RestClientResponseException cause) {
                assertThat(cause.getResponseBodyAsString()).isEmpty();
                assertThat(cause.getResponseHeaders().getFirst("X-Api-Key")).isNull();
            }
        });
        server.verify();
    }

    @Test
    void unknownSearch404IsNotReportedAsAnEmptySuccess() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND).body("not json"));
        assertThatThrownBy(() -> client.retrieveSearchBooks("책", 1)).isInstanceOfSatisfying(BookException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(BookErrorStatus.ALADIN_API_ERROR));
        server.verify();
    }

    @Test
    void transportFailureDoesNotExposeOriginalMessageOrCause() {
        server.expect(anything()).andRespond(withException(new IOException("test-only-secret private-query")));
        assertThatThrownBy(() -> client.retrieveSearchBooks("private-query", 1))
                .isInstanceOfSatisfying(BookException.class,
                        e -> assertThat(stackTrace(e)).doesNotContain("test-only-secret", "private-query"));
        server.verify();
    }

    @Test
    void missingKeyFailsWithoutMakingARequest() {
        properties.setApiKey("");
        assertThatThrownBy(client::retrieveRecommendedBooks).isInstanceOf(BookException.class);
        server.verify();
    }

    private String stackTrace(Throwable failure) {
        var writer = new StringWriter();
        failure.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private String payload(String items, int page, int size, int total) {
        return "{\"success\":true,\"data\":{\"items\":[" + items + "],\"currentPage\":" + page
                + ",\"pageSize\":" + size + ",\"totalCount\":" + total + "}}";
    }
}
