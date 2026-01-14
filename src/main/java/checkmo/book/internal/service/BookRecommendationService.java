package checkmo.book.internal.service;

import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.internal.util.DayOfWeekUtils;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
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

    private static final String REDIS_KEY = "book:recommendations:daily";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AladinApiService aladinApiService;

    public BookResponseDTO.BookList retrieveRecommendedBooks() {
        try {
            Object cachedData = redisTemplate.opsForValue().get(REDIS_KEY);

            if (cachedData instanceof BookResponseDTO.BookList recommendedBooks) {
                return recommendedBooks;
            }

            return refreshDailyRecommendedBooks();

        } catch (Exception e) {
            log.error("Redis에서 추천 책 조회 중 오류 발생. API에서 데이터를 가져오기.", e);
            return refreshDailyRecommendedBooks();
        }
    }

    public BookResponseDTO.BookList refreshDailyRecommendedBooks() {
        try {
            BookResponseDTO.BookList bookList = aladinApiService.retrieveRecommendedBooks();

            if (bookList == null || bookList.getDetailInfoList() == null || bookList.getDetailInfoList().isEmpty()) {
                log.error("알라딘 API에서 추천 책을 가져오지 못했습니다.");
                return createEmptyBookList();
            }

            // 요일에 따라 4권만 저장
            int startIndex = DayOfWeekUtils.calculateStartIndexByDayOfWeek();
            var allBooks = bookList.getDetailInfoList();

            if (allBooks.size() < startIndex + 4) {
                log.error("추천 책 목록 부족, 전체: {}개, 필요: {}개", allBooks.size(), startIndex + 4);
                return createEmptyBookList();
            }

            var booksForToday = allBooks.subList(startIndex, startIndex + 4);
            saveRecommendedBooks(booksForToday);

            // 저장한 4권을 BookList로 감싸서 반환
            return BookResponseDTO.BookList.builder()
                    .detailInfoList(booksForToday)
                    .hasNext(false)
                    .currentPage(null)
                    .build();

        } catch (Exception e) {
            log.error("API에서 추천 책 가져오기 중 오류", e);
            return createEmptyBookList();
        }
    }

    private void saveRecommendedBooks(List<DetailInfo> books) {
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

            redisTemplate.opsForValue().set(REDIS_KEY, bookList);
        } catch (Exception e) {
            log.error("Redis에 추천 책 저장 중 오류 발생", e);
        }
    }

    public boolean hasRecommendedBooks() {
        try {
            return redisTemplate.hasKey(REDIS_KEY);
        } catch (Exception e) {
            log.error("Redis 키 확인 중 오류 발생", e);
            return false;
        }
    }

    private BookResponseDTO.BookList createEmptyBookList() {
        return BookResponseDTO.BookList.builder()
                .detailInfoList(Collections.emptyList())
                .hasNext(false)
                .currentPage(null)
                .build();
    }
}
