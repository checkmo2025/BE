package checkmo.book.internal.service;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.web.dto.BookResponseDTO;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationRefreshGuard {
    public static final String DATA_KEY = "book:recommendations:{yes24-monthly-v1}:daily";
    public static final String DATE_KEY = DATA_KEY + ":updated_at";
    private static final String LOCK_KEY = DATA_KEY + ":lock";
    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) end return 0", Long.class);
    private static final DefaultRedisScript<Long> PUBLISH = publishScript();
    private final StringRedisTemplate redis;
    private final RedisTemplate<String, Object> objectRedis;
    private final Yes24Properties properties;

    public String acquire() {
        String token = UUID.randomUUID().toString();
        var retry = properties.getRecommendation().getRefresh().getRetry();
        long lease = 4L * BookRecommendationService.MAX_CANDIDATES_PER_CATEGORY * retry.getAttempts() * (2L * properties.getSearch().getTimeoutMs()
                + 1500 + retry.getMaxBackoff().toMillis()) + 10000;
        try {
            if (Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(LOCK_KEY, token, Duration.ofMillis(lease)))) {
                return token;
            }
        } catch (RuntimeException ignored) {
            // 공유 잠금이 없으면 호출하지 않는다.
        }
        throw new BookException(BookErrorStatus.YES24_API_ERROR);
    }

    @SuppressWarnings("unchecked")
    public void publish(String token, BookResponseDTO.BookList books, String day) {
        var serializer = (RedisSerializer<Object>) objectRedis.getValueSerializer();
        Long written = redis.execute(PUBLISH, List.of(LOCK_KEY, DATA_KEY, DATE_KEY), token,
                new String(serializer.serialize(books), StandardCharsets.UTF_8),
                new String(serializer.serialize(day), StandardCharsets.UTF_8));
        if (!Long.valueOf(1).equals(written)) throw new BookException(BookErrorStatus.YES24_API_ERROR);
    }

    public void release(String token) {
        try {
            redis.execute(RELEASE, List.of(LOCK_KEY), token);
        } catch (RuntimeException ignored) {
            // 임대 만료가 복구한다. 다른 소유자의 잠금은 삭제하지 않는다.
        }
    }

    private static DefaultRedisScript<Long> publishScript() {
        var script = new DefaultRedisScript<Long>();
        script.setLocation(new ClassPathResource("redis/recommendation-publish.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
