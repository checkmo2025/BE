package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class BookSearchCacheServiceTest {

    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private BookSearchCacheService bookSearchCacheService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        bookSearchCacheService = new BookSearchCacheService(redisTemplate);
    }

    @Test
    void buildKeyNormalizesKeywordAndIncludesMaxResultsAndPage() {
        String normalizedKey = bookSearchCacheService.buildKey("  Java   Spring ", 10, 1);

        assertThat(bookSearchCacheService.buildKey("java spring", 10, 1)).isEqualTo(normalizedKey);
        assertThat(bookSearchCacheService.buildKey("java spring", 10, 2)).isNotEqualTo(normalizedKey);
        assertThat(bookSearchCacheService.buildKey("java spring", 20, 1)).isNotEqualTo(normalizedKey);
        assertThat(normalizedKey)
                .startsWith("book:search:v1:")
                .doesNotContain("Java")
                .doesNotContain("java")
                .doesNotContain("Spring")
                .doesNotContain("spring");
    }

    @Test
    void saveStoresRawBookListForTenMinutes() {
        BookResponseDTO.BookList bookList = bookList(true);

        bookSearchCacheService.save("java", 10, 1, bookList);

        verify(valueOperations).set(
                eq(bookSearchCacheService.buildKey("java", 10, 1)),
                any(BookResponseDTO.BookList.class),
                eq(Duration.ofMinutes(10))
        );
    }

    @Test
    void saveStripsLikedByMeBeforeCaching() {
        BookResponseDTO.BookList bookList = bookList(true);

        bookSearchCacheService.save("java", 10, 1, bookList);

        org.mockito.ArgumentCaptor<BookResponseDTO.BookList> captor =
                org.mockito.ArgumentCaptor.forClass(BookResponseDTO.BookList.class);
        verify(valueOperations).set(eq(bookSearchCacheService.buildKey("java", 10, 1)), captor.capture(), any(Duration.class));

        assertThat(captor.getValue().getTotalResults()).isEqualTo(37);
        assertThat(captor.getValue().getDetailInfoList())
                .extracting(DetailInfo::isLikedByMe)
                .containsOnly(false);
    }

    private BookResponseDTO.BookList bookList(boolean likedByMe) {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(List.of(DetailInfo.builder()
                        .isbn("9791169213882")
                        .title("테스트 책")
                        .author("테스트 저자")
                        .imgUrl("https://example.com/book.jpg")
                        .publisher("테스트 출판사")
                        .description("테스트 설명")
                        .link("https://example.com/book")
                        .likedByMe(likedByMe)
                        .build()))
                .hasNext(true)
                .currentPage(1)
                .totalResults(37)
                .build();
    }
}
