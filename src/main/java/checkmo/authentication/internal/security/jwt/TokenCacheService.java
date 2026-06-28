package checkmo.authentication.internal.security.jwt;

import checkmo.authentication.internal.config.properties.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken::";
    private static final String BLACKLIST_PREFIX = "blacklist::";
    private static final String OAUTH_CODE_PREFIX = "oauthCode::";
    private static final long OAUTH_CODE_TTL_MS = 120_000L; // 앱 소셜 로그인 일회용 코드 2분
    private static final String CONSUME_OAUTH_CODE_SCRIPT = """
            local v = redis.call('GET', KEYS[1])
            if v then redis.call('DEL', KEYS[1]) end
            return v
            """;
    private static final String COMPARE_AND_ROTATE_REFRESH_TOKEN_SCRIPT = """
            local current = redis.call('GET', KEYS[1])
            if current == ARGV[1] or current == ARGV[2] then
                redis.call('SET', KEYS[1], ARGV[3], 'PX', ARGV[4])
                return 1
            end
            return 0
            """;
    private static final String DELETE_REFRESH_TOKEN_IF_MATCHES_SCRIPT = """
            local current = redis.call('GET', KEYS[1])
            if current == ARGV[1] or current == ARGV[2] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            return 0
            """;
    private static final StringRedisSerializer STRING_SERIALIZER = new StringRedisSerializer();

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    public void saveRefreshToken(String userId, String refreshToken) {
        log.info("리프레시 토큰 저장 - userId={}", userId);
        long ttlMs = jwtProperties.getTokenValidity().getRefreshToken();
        byte[] key = serialize(REFRESH_TOKEN_PREFIX + userId);
        byte[] value = serialize(refreshToken);
        redisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.stringCommands().set(
                        key,
                        value,
                        Expiration.milliseconds(ttlMs),
                        SetOption.upsert()
                )
        );
    }

    public String getRefreshToken(String userId) {
        log.info("리프레시 토큰 조회 - userId={}", userId);
        byte[] key = serialize(REFRESH_TOKEN_PREFIX + userId);
        byte[] value = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                connection.stringCommands().get(key)
        );
        return value == null ? null : deserializeRefreshToken(value);
    }

    public boolean compareAndRotateRefreshToken(
            String userId,
            String expectedRefreshToken,
            String newRefreshToken,
            Duration ttl
    ) {
        log.info("리프레시 토큰 원자적 회전 - userId={}", userId);
        byte[] key = serialize(REFRESH_TOKEN_PREFIX + userId);
        byte[] expected = serialize(expectedRefreshToken);
        byte[] legacyExpected = serializeLegacyValue(expectedRefreshToken);
        byte[] next = serialize(newRefreshToken);
        byte[] ttlMs = serialize(String.valueOf(ttl.toMillis()));
        return executeBooleanScript(COMPARE_AND_ROTATE_REFRESH_TOKEN_SCRIPT, key, expected, legacyExpected, next, ttlMs);
    }

    public void deleteRefreshToken(String userId) {
        log.info("리프레시 토큰 삭제 - userId={}", userId);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    public boolean deleteRefreshTokenIfMatches(String userId, String expectedRefreshToken) {
        log.info("리프레시 토큰 일치 시 삭제 - userId={}", userId);
        byte[] key = serialize(REFRESH_TOKEN_PREFIX + userId);
        byte[] expected = serialize(expectedRefreshToken);
        byte[] legacyExpected = serializeLegacyValue(expectedRefreshToken);
        return executeBooleanScript(DELETE_REFRESH_TOKEN_IF_MATCHES_SCRIPT, key, expected, legacyExpected);
    }

    // 앱 소셜 로그인 일회용 코드 저장 (단기 TTL). value는 "isProfileCompleted|refreshToken" 형태.
    public void saveOAuthExchangeCode(String code, String value) {
        byte[] key = serialize(OAUTH_CODE_PREFIX + code);
        redisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.stringCommands().set(
                        key,
                        serialize(value),
                        Expiration.milliseconds(OAUTH_CODE_TTL_MS),
                        SetOption.upsert()
                )
        );
    }

    // 일회용 코드를 원자적으로 조회+삭제 (1회만 사용 가능). 없으면 null.
    public String consumeOAuthExchangeCode(String code) {
        byte[] key = serialize(OAUTH_CODE_PREFIX + code);
        byte[] value = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                connection.scriptingCommands().eval(
                        CONSUME_OAUTH_CODE_SCRIPT.getBytes(StandardCharsets.UTF_8),
                        ReturnType.VALUE,
                        1,
                        key
                )
        );
        return value == null ? null : STRING_SERIALIZER.deserialize(value);
    }

    private boolean executeBooleanScript(String script, byte[] key, byte[]... args) {
        byte[][] keysAndArgs = new byte[args.length + 1][];
        keysAndArgs[0] = key;
        System.arraycopy(args, 0, keysAndArgs, 1, args.length);

        Long result = redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.scriptingCommands().eval(
                        script.getBytes(StandardCharsets.UTF_8),
                        ReturnType.INTEGER,
                        1,
                        keysAndArgs
                )
        );
        return Long.valueOf(1L).equals(result);
    }

    private byte[] serialize(String value) {
        return STRING_SERIALIZER.serialize(value);
    }

    private String deserializeRefreshToken(byte[] value) {
        Object legacyValue = deserializeLegacyValue(value);
        if (legacyValue instanceof String refreshToken) {
            return refreshToken;
        }
        return STRING_SERIALIZER.deserialize(value);
    }

    private Object deserializeLegacyValue(byte[] value) {
        RedisSerializer<?> valueSerializer = redisTemplate.getValueSerializer();
        if (valueSerializer == null) {
            return null;
        }
        try {
            return valueSerializer.deserialize(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private byte[] serializeLegacyValue(String value) {
        RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
        return valueSerializer == null ? serialize(value) : valueSerializer.serialize(value);
    }

    // 블랙리스트 토큰 저장
    @CachePut(value = "blacklist", key = "#accessToken")
    public String saveBlacklistToken(String accessToken) {
        log.info("블랙리스트 토큰 저장");
        return accessToken;
    }

    // 블랙리스트 토큰 조회
    public boolean isAccessTokenBlacklisted(String accessToken) {
        log.info("블랙리스트 토큰 조회");
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
