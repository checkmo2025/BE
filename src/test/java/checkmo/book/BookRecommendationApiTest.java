package checkmo.book;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.book.internal.service.AladinRecommendationRefreshClient;
import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.support.ApiTestSupport;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class BookRecommendationApiTest extends ApiTestSupport {

    private static final String REDIS_KEY = "book:recommendations:daily";
    private static final String REDIS_UPDATED_AT_KEY = "book:recommendations:daily:updated_at";

    @MockitoBean
    AladinRecommendationRefreshClient recommendationRefreshClient;

    @MockitoBean
    AladinApiService aladinApiService;

    @Test
    void recommendBooksReturnsStaleCacheWhenAladinRefreshFails() {
        TestUser user = createUser();
        BookResponseDTO.BookList staleCache = bookList("stale", 4);

        when(redisValueOperations.get(REDIS_KEY)).thenReturn(staleCache);
        when(redisValueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now().minusDays(1).toString());
        when(recommendationRefreshClient.retrieveRecommendedBooks())
                .thenThrow(new RuntimeException("aladin unavailable"));
        when(aladinApiService.applyLikedByMe(staleCache, Long.valueOf(user.id()))).thenReturn(staleCache);

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/books/recommend")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("code", equalTo("COMMON200"))
                .body("result.detailInfoList.size()", equalTo(4));

        verify(recommendationRefreshClient).retrieveRecommendedBooks();
    }

    @Test
    void recommendBooksReturnsBook500WhenNoCacheAndAladinRefreshFails() {
        TestUser user = createUser();

        when(redisValueOperations.get(REDIS_KEY)).thenReturn(null);
        when(recommendationRefreshClient.retrieveRecommendedBooks())
                .thenThrow(new RuntimeException("aladin unavailable"));

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/books/recommend")
                .then()
                .statusCode(500)
                .body("isSuccess", equalTo(false))
                .body("code", equalTo("BOOK_500"));
    }

    @Test
    void recommendBooksReturnsFreshCacheWithoutRefreshingAladin() {
        TestUser user = createUser();
        BookResponseDTO.BookList freshCache = bookList("fresh", 4);

        when(redisValueOperations.get(REDIS_KEY)).thenReturn(freshCache);
        when(redisValueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now().toString());
        when(aladinApiService.applyLikedByMe(freshCache, Long.valueOf(user.id()))).thenReturn(freshCache);

        given()
                .cookie(accessTokenCookie(user))
                .when()
                .get("/api/v1/books/recommend")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("code", equalTo("COMMON200"))
                .body("result.detailInfoList.size()", equalTo(4));

        verify(recommendationRefreshClient, never()).retrieveRecommendedBooks();
    }

    private static BookResponseDTO.BookList bookList(String prefix, int size) {
        List<DetailInfo> books = IntStream.rangeClosed(1, size)
                .mapToObj(index -> DetailInfo.builder()
                        .isbn(prefix + "-isbn-" + index)
                        .title("추천 책 " + index)
                        .author("테스트 저자")
                        .imgUrl("https://example.com/book-" + index + ".jpg")
                        .publisher("테스트 출판사")
                        .description("테스트 설명")
                        .link("https://example.com/book-" + index)
                        .build())
                .toList();

        return BookResponseDTO.BookList.builder()
                .detailInfoList(books)
                .hasNext(false)
                .currentPage(null)
                .totalResults(size)
                .build();
    }
}
