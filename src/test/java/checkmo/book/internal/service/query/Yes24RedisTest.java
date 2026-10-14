package checkmo.book.internal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.exception.BookException;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@EnabledIfEnvironmentVariable(named = "YES24_TEST_REDIS_PORT", matches = "[0-9]+")
class Yes24RedisTest {
    private LettuceConnectionFactory factory;
    private StringRedisTemplate redis;
    private Yes24Properties properties;
    private Yes24RateLimiter limiter;

    @BeforeEach
    void setUp() {
        factory = new LettuceConnectionFactory("127.0.0.1", Integer.parseInt(System.getenv("YES24_TEST_REDIS_PORT")));
        factory.afterPropertiesSet();
        redis = new StringRedisTemplate(factory);
        properties = new Yes24Properties();
        properties.setAccountScope("test-" + UUID.randomUUID());
        limiter = new Yes24RateLimiter(redis, properties);
    }

    @AfterEach
    void close() { factory.destroy(); }

    @Test
    void sharesDailyBudgetAcrossClientsAndResetsOnKstDayChange() {
        properties.setRequestsPerDay(1);
        limiter.acquire();
        var otherInstance = new Yes24RateLimiter(redis, properties);
        assertThatThrownBy(otherInstance::acquire).isInstanceOf(BookException.class);
        redis.opsForHash().put("yes24:account:" + properties.getAccountScope(), "day", "0");
        otherInstance.acquire();
    }

    @Test
    void cooldownPreventsAllInstancesFromRequestingEarly() {
        limiter.defer("10");
        assertThatThrownBy(new Yes24RateLimiter(redis, properties)::acquire).isInstanceOf(BookException.class);
    }

    @Test
    void concurrentLuaRequestsOnlyAdmitOneInTheSpacingWindow() {
        var script = new DefaultRedisScript<Long>();
        script.setLocation(new ClassPathResource("redis/yes24-permit.lua"));
        script.setResultType(Long.class);
        var key = "yes24:test:" + UUID.randomUUID();
        var results = IntStream.range(0, 20).parallel()
                .mapToObj(i -> redis.execute(script, List.of(key), "10000", "20000")).toList();
        assertThat(results).filteredOn(value -> value == 0).hasSize(1);
        assertThat(results).filteredOn(value -> value > 0).hasSize(19);
    }
}
