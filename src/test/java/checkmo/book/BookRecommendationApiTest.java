package checkmo.book;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.book.internal.service.Yes24RecommendationRefreshClient;
import checkmo.book.internal.service.query.Yes24ApiService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import checkmo.support.ApiTestSupport;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class BookRecommendationApiTest extends ApiTestSupport {

    private static final String REDIS_KEY = "book:recommendations:{yes24}:daily";
    private static final String REDIS_UPDATED_AT_KEY = "book:recommendations:{yes24}:daily:updated_at";

    @MockitoBean
    Yes24RecommendationRefreshClient recommendationRefreshClient;

    @MockitoBean
    Yes24ApiService yes24ApiService;

    @BeforeEach
    void mockAtomicCacheRead() {
        when(redisValueOperations.multiGet(List.of(REDIS_KEY, REDIS_UPDATED_AT_KEY)))
                .thenAnswer(invocation -> Arrays.asList(redisValueOperations.get(REDIS_KEY), redisValueOperations.get(REDIS_UPDATED_AT_KEY)));
    }

    @Test
    void recommendBooksReturnsStaleCacheWhenYes24RefreshFails() {
        TestUser user = createUser();
        BookResponseDTO.BookList staleCache = bookList("stale", 4);

        when(redisValueOperations.get(REDIS_KEY)).thenReturn(staleCache);
        when(redisValueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now().minusDays(1).toString());
        when(recommendationRefreshClient.retrieveRecommendedBooks())
                .thenThrow(new RuntimeException("yes24 unavailable"));
        when(yes24ApiService.applyLikedByMe(staleCache, Long.valueOf(user.id()))).thenReturn(staleCache);

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
    void recommendBooksReturnsBook500WhenNoCacheAndYes24RefreshFails() {
        TestUser user = createUser();

        when(redisValueOperations.get(REDIS_KEY)).thenReturn(null);
        when(recommendationRefreshClient.retrieveRecommendedBooks())
                .thenThrow(new RuntimeException("yes24 unavailable"));

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
    void recommendBooksReturnsFreshCacheWithoutRefreshingYes24() {
        TestUser user = createUser();
        BookResponseDTO.BookList freshCache = bookList("fresh", 4);

        when(redisValueOperations.get(REDIS_KEY)).thenReturn(freshCache);
        when(redisValueOperations.get(REDIS_UPDATED_AT_KEY)).thenReturn(LocalDate.now().toString());
        when(yes24ApiService.applyLikedByMe(freshCache, Long.valueOf(user.id()))).thenReturn(freshCache);

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
