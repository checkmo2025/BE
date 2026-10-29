package checkmo.book.internal.service.query;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Yes24RateLimiter {
    private static final DefaultRedisScript<Long> PERMIT = script("yes24-permit.lua");
    private static final DefaultRedisScript<Long> COOLDOWN = script("yes24-cooldown.lua");
    private final StringRedisTemplate redis;
    private final Yes24Properties properties;

    public void acquire() {
        long deadline = System.nanoTime() + Duration.ofMillis(1500).toNanos();
        while (true) {
            // Redis 시계를 사용하므로 인스턴스별 시간 오차·키 교체로 예산이 분리되지 않는다.
            Long wait = execute(PERMIT, Long.toString(1000L / properties.getRequestsPerSecond() + 1),
                    Integer.toString(properties.getRequestsPerDay()));
            if (wait != null && wait == 0) return;
            if (wait == null || wait < 0 || wait > 1500
                    || System.nanoTime() + Duration.ofMillis(wait).toNanos() > deadline) {
                throw new BookException(BookErrorStatus.YES24_API_ERROR);
            }
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BookException(BookErrorStatus.YES24_API_ERROR);
            }
        }
    }

    public void defer(String retryAfter) {
        execute(COOLDOWN, Long.toString(retryDelayMillis(retryAfter, Instant.now())));
    }

    static long retryDelayMillis(String value, Instant now) {
        try {
            if (value != null && value.matches("[0-9]{1,9}")) {
                return Math.max(1000, Long.parseLong(value) * 1000);
            }
            return Math.max(1000, Duration.between(now,
                    ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()).toMillis());
        } catch (RuntimeException ignored) {
            return 1000;
        }
    }

    private Long execute(DefaultRedisScript<Long> script, String... args) {
        try {
            return redis.execute(script, List.of("yes24:account:" + properties.getAccountScope()), (Object[]) args);
        } catch (RuntimeException e) {
            // Redis 장애 때 무제한 호출로 전환하지 않는다. 상위 계층이 기존 캐시/DB로 대체한다.
            throw new BookException(BookErrorStatus.YES24_API_ERROR);
        }
    }

    private static DefaultRedisScript<Long> script(String name) {
        var script = new DefaultRedisScript<Long>();
        script.setLocation(new ClassPathResource("redis/" + name));
        script.setResultType(Long.class);
        return script;
    }
}
