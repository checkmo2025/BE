package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.exception.BookException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

class Yes24RateLimiterTest {
    @Test
    @SuppressWarnings("unchecked")
    void deniesExhaustedBudgetAndRedisFailureWithoutCallingThrough() {
        var redis = mock(StringRedisTemplate.class);
        var limiter = new Yes24RateLimiter(redis, new Yes24Properties());
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(-1L).thenReturn(null).thenThrow(new IllegalStateException("redis down"));
        for (int i = 0; i < 3; i++) assertThatThrownBy(limiter::acquire).isInstanceOf(BookException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void waitsForPermitButRejectsLongCooldown() {
        var redis = mock(StringRedisTemplate.class);
        var limiter = new Yes24RateLimiter(redis, new Yes24Properties());
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(1L, 0L, 2000L);
        limiter.acquire();
        assertThatThrownBy(limiter::acquire).isInstanceOf(BookException.class);
    }

    @Test
    void understandsRetryAfterSecondsAndHttpDate() {
        Instant now = Instant.parse("2026-10-14T00:00:00Z");
        assertThat(Yes24RateLimiter.retryDelayMillis("20", now)).isEqualTo(20000);
        assertThat(Yes24RateLimiter.retryDelayMillis("Wed, 14 Oct 2026 00:00:30 GMT", now)).isEqualTo(30000);
        assertThat(Yes24RateLimiter.retryDelayMillis(null, now)).isEqualTo(1000);
        assertThat(Yes24RateLimiter.retryDelayMillis("invalid", now)).isEqualTo(1000);
    }
}
