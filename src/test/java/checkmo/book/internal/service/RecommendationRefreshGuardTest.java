package checkmo.book.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.book.internal.config.properties.Yes24Properties;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RecommendationRefreshGuardTest {
    @Test
    @SuppressWarnings("unchecked")
    void leaseCoversFourCategoriesFiveCandidatesAndEachRequestRetry() {
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        var properties = new Yes24Properties();
        var guard = new RecommendationRefreshGuard(redis, null, properties);
        assertThat(guard.acquire()).isNotBlank();
        var lease = ArgumentCaptor.forClass(Duration.class);
        verify(values).setIfAbsent(anyString(), anyString(), lease.capture());
        long expected = 4L * 5 * 3 * (2 * 15000 + 1500 + 5000) + 10000;
        assertThat(lease.getValue()).isEqualTo(Duration.ofMillis(expected));
    }
}
