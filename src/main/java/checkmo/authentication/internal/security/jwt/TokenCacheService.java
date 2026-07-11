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
    private static final String REFRESH_TOKEN_SESSION_INDEX_PREFIX = "refreshTokenSessions::";
    private static final String BLACKLIST_PREFIX = "blacklist::";
    private static final String OAUTH_CODE_PREFIX = "oauthCode::";
    private static final long OAUTH_CODE_TTL_MS = 120_000L;
    private static final String SAVE_REFRESH_TOKEN_SCRIPT = """
            local function pruneMissingSessionKeys(sessionIndexKey)
                local sessionKeys = redis.call('SMEMBERS', sessionIndexKey)
                for _, sessionKey in ipairs(sessionKeys) do
                    if redis.call('EXISTS', sessionKey) == 0 then
                        redis.call('SREM', sessionIndexKey, sessionKey)
                    end
                end
            end

            redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
            redis.call('SADD', KEYS[2], KEYS[1])
            pruneMissingSessionKeys(KEYS[2])
            redis.call('PEXPIRE', KEYS[2], ARGV[2])
            return 1
            """;
    private static final String COMPARE_AND_ROTATE_REFRESH_TOKEN_SCRIPT = """
            local function pruneMissingSessionKeys(sessionIndexKey)
                local sessionKeys = redis.call('SMEMBERS', sessionIndexKey)
                for _, sessionKey in ipairs(sessionKeys) do
                    if redis.call('EXISTS', sessionKey) == 0 then
                        redis.call('SREM', sessionIndexKey, sessionKey)
                    end
                end
            end

            local current = redis.call('GET', KEYS[1])
            if current == ARGV[1] or current == ARGV[2] then
                redis.call('SET', KEYS[1], ARGV[3], 'PX', ARGV[4])
                redis.call('SADD', KEYS[3], KEYS[1])
                pruneMissingSessionKeys(KEYS[3])
                redis.call('PEXPIRE', KEYS[3], ARGV[4])
                return 1
            end

            local legacy = redis.call('GET', KEYS[2])
            if legacy == ARGV[1] or legacy == ARGV[2] then
                redis.call('SET', KEYS[1], ARGV[3], 'PX', ARGV[4])
                redis.call('DEL', KEYS[2])
                redis.call('SADD', KEYS[3], KEYS[1])
                pruneMissingSessionKeys(KEYS[3])
                redis.call('PEXPIRE', KEYS[3], ARGV[4])
                return 1
            end
            return 0
            """;
    private static final String DELETE_REFRESH_TOKEN_IF_MATCHES_SCRIPT = """
            local function cleanupSessionIndex(sessionIndexKey)
                local sessionKeys = redis.call('SMEMBERS', sessionIndexKey)
                for _, sessionKey in ipairs(sessionKeys) do
                    if redis.call('EXISTS', sessionKey) == 0 then
                        redis.call('SREM', sessionIndexKey, sessionKey)
                    end
                end
                if redis.call('SCARD', sessionIndexKey) == 0 then
                    redis.call('DEL', sessionIndexKey)
                end
            end

            local current = redis.call('GET', KEYS[1])
            if current == ARGV[1] or current == ARGV[2] then
                redis.call('DEL', KEYS[1])
                redis.call('SREM', KEYS[3], KEYS[1])
                cleanupSessionIndex(KEYS[3])
                return 1
            end

            local legacy = redis.call('GET', KEYS[2])
            if legacy == ARGV[1] or legacy == ARGV[2] then
                redis.call('DEL', KEYS[2])
                cleanupSessionIndex(KEYS[3])
                return 1
            end
            return 0
            """;
    private static final String DELETE_ALL_REFRESH_TOKENS_SCRIPT = """
            local sessionKeys = redis.call('SMEMBERS', KEYS[1])
            for _, sessionKey in ipairs(sessionKeys) do
                redis.call('DEL', sessionKey)
            end
            redis.call('DEL', KEYS[1])
            redis.call('DEL', KEYS[2])
            return #sessionKeys
            """;
    private static final String CONSUME_OAUTH_CODE_SCRIPT = """
            local value = redis.call('GET', KEYS[1])
            if value then
                redis.call('DEL', KEYS[1])
            end
            return value
            """;
    private static final StringRedisSerializer STRING_SERIALIZER = new StringRedisSerializer();

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    public void saveRefreshToken(Long userId, String sessionId, String refreshToken) {
        saveRefreshToken(String.valueOf(userId), sessionId, refreshToken);
    }

    public void saveRefreshToken(String userId, String sessionId, String refreshToken) {
        log.info("리프레시 토큰 저장 - userId={}, sessionId={}", userId, sessionId);
        long ttlMs = jwtProperties.getTokenValidity().getRefreshToken();
        byte[] sessionKey = serialize(refreshTokenSessionKey(userId, sessionId));
        byte[] sessionIndexKey = serialize(refreshTokenSessionIndexKey(userId));
        byte[] value = serialize(refreshToken);
        byte[] ttl = serialize(String.valueOf(ttlMs));
        executeBooleanScript(
                SAVE_REFRESH_TOKEN_SCRIPT,
                new byte[][]{sessionKey, sessionIndexKey},
                value,
                ttl
        );
    }

    public String getRefreshToken(Long userId, String sessionId) {
        return getRefreshToken(String.valueOf(userId), sessionId);
    }

    public String getRefreshToken(String userId, String sessionId) {
        log.info("리프레시 토큰 조회 - userId={}, sessionId={}", userId, sessionId);
        byte[] sessionKey = serialize(refreshTokenSessionKey(userId, sessionId));
        byte[] legacyKey = serialize(legacyRefreshTokenKey(userId));
        byte[] value = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                {
                    byte[] sessionValue = connection.stringCommands().get(sessionKey);
                    return sessionValue != null
                            ? sessionValue
                            : connection.stringCommands().get(legacyKey);
                }
        );
        return value == null ? null : deserializeRefreshToken(value);
    }

    public boolean compareAndRotateRefreshToken(
            Long userId,
            String sessionId,
            String expectedRefreshToken,
            String newRefreshToken,
            Duration ttl
    ) {
        return compareAndRotateRefreshToken(
                String.valueOf(userId),
                sessionId,
                expectedRefreshToken,
                newRefreshToken,
                ttl
        );
    }

    public boolean compareAndRotateRefreshToken(
            String userId,
            String sessionId,
            String expectedRefreshToken,
            String newRefreshToken,
            Duration ttl
    ) {
        log.info("리프레시 토큰 원자적 회전 - userId={}, sessionId={}", userId, sessionId);
        byte[] sessionKey = serialize(refreshTokenSessionKey(userId, sessionId));
        byte[] legacyKey = serialize(legacyRefreshTokenKey(userId));
        byte[] sessionIndexKey = serialize(refreshTokenSessionIndexKey(userId));
        byte[] expected = serialize(expectedRefreshToken);
        byte[] legacyExpected = serializeLegacyValue(expectedRefreshToken);
        byte[] next = serialize(newRefreshToken);
        byte[] ttlMs = serialize(String.valueOf(ttl.toMillis()));
        return executeBooleanScript(
                COMPARE_AND_ROTATE_REFRESH_TOKEN_SCRIPT,
                new byte[][]{sessionKey, legacyKey, sessionIndexKey},
                expected,
                legacyExpected,
                next,
                ttlMs
        );
    }

    public void deleteRefreshToken(Long userId) {
        deleteRefreshToken(String.valueOf(userId));
    }

    public void deleteRefreshToken(String userId) {
        log.info("회원의 모든 리프레시 토큰 삭제 - userId={}", userId);
        byte[] sessionIndexKey = serialize(refreshTokenSessionIndexKey(userId));
        byte[] legacyKey = serialize(legacyRefreshTokenKey(userId));
        redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.scriptingCommands().eval(
                        DELETE_ALL_REFRESH_TOKENS_SCRIPT.getBytes(StandardCharsets.UTF_8),
                        ReturnType.INTEGER,
                        2,
                        sessionIndexKey,
                        legacyKey
                )
        );
    }

    public boolean deleteRefreshTokenIfMatches(Long userId, String sessionId, String expectedRefreshToken) {
        return deleteRefreshTokenIfMatches(String.valueOf(userId), sessionId, expectedRefreshToken);
    }

    public boolean deleteRefreshTokenIfMatches(String userId, String sessionId, String expectedRefreshToken) {
        log.info("리프레시 토큰 일치 시 삭제 - userId={}, sessionId={}", userId, sessionId);
        byte[] sessionKey = serialize(refreshTokenSessionKey(userId, sessionId));
        byte[] legacyKey = serialize(legacyRefreshTokenKey(userId));
        byte[] sessionIndexKey = serialize(refreshTokenSessionIndexKey(userId));
        byte[] expected = serialize(expectedRefreshToken);
        byte[] legacyExpected = serializeLegacyValue(expectedRefreshToken);
        return executeBooleanScript(
                DELETE_REFRESH_TOKEN_IF_MATCHES_SCRIPT,
                new byte[][]{sessionKey, legacyKey, sessionIndexKey},
                expected,
                legacyExpected
        );
    }

    public void saveOAuthExchangeCode(String code, String value) {
        byte[] key = serialize(OAUTH_CODE_PREFIX + code);
        byte[] serializedValue = serialize(value);
        redisTemplate.execute((RedisCallback<Boolean>) connection ->
                connection.stringCommands().set(
                        key,
                        serializedValue,
                        Expiration.milliseconds(OAUTH_CODE_TTL_MS),
                        SetOption.upsert()
                )
        );
    }

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

    private boolean executeBooleanScript(String script, byte[][] keys, byte[]... args) {
        byte[][] keysAndArgs = new byte[keys.length + args.length][];
        System.arraycopy(keys, 0, keysAndArgs, 0, keys.length);
        System.arraycopy(args, 0, keysAndArgs, keys.length, args.length);

        Long result = redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.scriptingCommands().eval(
                        script.getBytes(StandardCharsets.UTF_8),
                        ReturnType.INTEGER,
                        keys.length,
                        keysAndArgs
                )
        );
        return Long.valueOf(1L).equals(result);
    }

    private String legacyRefreshTokenKey(String userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }

    private String refreshTokenSessionKey(String userId, String sessionId) {
        return legacyRefreshTokenKey(userId) + "::" + sessionId;
    }

    private String refreshTokenSessionIndexKey(String userId) {
        return REFRESH_TOKEN_SESSION_INDEX_PREFIX + userId;
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
