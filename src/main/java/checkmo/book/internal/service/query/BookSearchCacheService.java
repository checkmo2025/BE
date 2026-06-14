package checkmo.book.internal.service.query;

import checkmo.book.web.dto.BookResponseDTO;
import checkmo.book.web.dto.BookResponseDTO.DetailInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchCacheService {

    private static final String KEY_PREFIX = "book:search:v1:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<BookResponseDTO.BookList> retrieve(String keyword, int maxResults, int page) {
        try {
            Object cachedData = redisTemplate.opsForValue().get(buildKey(keyword, maxResults, page));
            if (cachedData instanceof BookResponseDTO.BookList bookList) {
                return Optional.of(bookList);
            }
        } catch (Exception e) {
            log.warn("책 검색 캐시 조회 중 오류 발생. keywordHash={}, page={}", keywordHashForLog(keyword), page, e);
        }
        return Optional.empty();
    }

    public void save(String keyword, int maxResults, int page, BookResponseDTO.BookList bookList) {
        if (bookList == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(
                    buildKey(keyword, maxResults, page),
                    withoutLikedByMe(bookList),
                    TTL
            );
        } catch (Exception e) {
            log.warn("책 검색 캐시 저장 중 오류 발생. keywordHash={}, page={}", keywordHashForLog(keyword), page, e);
        }
    }

    String buildKey(String keyword, int maxResults, int page) {
        return KEY_PREFIX + sha256(normalizedPayload(keyword, maxResults, page));
    }

    BookResponseDTO.BookList withoutLikedByMe(BookResponseDTO.BookList bookList) {
        List<DetailInfo> details = Optional.ofNullable(bookList.getDetailInfoList())
                .orElseGet(List::of)
                .stream()
                .map(detail -> DetailInfo.builder()
                        .isbn(detail.getIsbn())
                        .title(detail.getTitle())
                        .author(detail.getAuthor())
                        .imgUrl(detail.getImgUrl())
                        .publisher(detail.getPublisher())
                        .description(detail.getDescription())
                        .link(detail.getLink())
                        .likedByMe(false)
                        .build())
                .toList();

        return BookResponseDTO.BookList.builder()
                .detailInfoList(details)
                .hasNext(bookList.isHasNext())
                .currentPage(bookList.getCurrentPage())
                .totalResults(bookList.getTotalResults())
                .build();
    }

    private String normalizedPayload(String keyword, int maxResults, int page) {
        String normalizedKeyword = Optional.ofNullable(keyword)
                .orElse("")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        return normalizedKeyword + "|" + maxResults + "|" + page;
    }

    private String keywordHashForLog(String keyword) {
        return sha256(Optional.ofNullable(keyword).orElse("").trim().toLowerCase(Locale.ROOT));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
