package checkmo.authentication.internal.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import checkmo.authentication.internal.config.properties.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@ExtendWith(MockitoExtension.class)
class TokenCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private TokenCacheService tokenCacheService;

    @Test
    void compareAndRotateRefreshTokenUsesRawLuaArguments() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(1_234L);
        RedisSerializer<Object> legacySerializer = legacyValueSerializer();
        doReturn(legacySerializer).when(redisTemplate).getValueSerializer();

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisScriptingCommands scriptingCommands = mock(RedisScriptingCommands.class, evalInvocation -> {
                if ("eval".equals(evalInvocation.getMethod().getName())) {
                    return 1L;
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(evalInvocation);
            });
            when(connection.scriptingCommands()).thenReturn(scriptingCommands);

            Object result = callback.doInRedis(connection);

            ArgumentCaptor<byte[][]> keysAndArgsCaptor = ArgumentCaptor.forClass(byte[][].class);
            verify(scriptingCommands).eval(
                    any(byte[].class),
                    org.mockito.ArgumentMatchers.eq(ReturnType.INTEGER),
                    org.mockito.ArgumentMatchers.eq(3),
                    keysAndArgsCaptor.capture()
            );
            byte[][] keysAndArgs = keysAndArgsCaptor.getValue();
            assertThat(Arrays.stream(keysAndArgs)
                    .map(value -> new String(value, StandardCharsets.UTF_8))
                    .toList())
                    .containsExactly(
                            "refreshToken::member-1::session-1",
                            "refreshToken::member-1",
                            "refreshTokenSessions::member-1",
                            "old-token",
                            new String(legacySerializer.serialize("old-token"), StandardCharsets.UTF_8),
                            "new-token",
                            "1234"
                    );
            return result;
        });

        boolean rotated = tokenCacheService.compareAndRotateRefreshToken(
                "member-1",
                "session-1",
                "old-token",
                "new-token",
                Duration.ofMillis(1_234L)
        );

        assertThat(rotated).isTrue();
    }

    @Test
    void compareAndRotateRefreshTokenSendsLegacySerializedExpectedTokenToLua() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(1_234L);
        RedisSerializer<Object> legacySerializer = legacyValueSerializer();
        doReturn(legacySerializer).when(redisTemplate).getValueSerializer();

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisScriptingCommands scriptingCommands = mock(RedisScriptingCommands.class, evalInvocation -> {
                if ("eval".equals(evalInvocation.getMethod().getName())) {
                    return 1L;
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(evalInvocation);
            });
            when(connection.scriptingCommands()).thenReturn(scriptingCommands);

            Object result = callback.doInRedis(connection);

            ArgumentCaptor<byte[][]> keysAndArgsCaptor = ArgumentCaptor.forClass(byte[][].class);
            verify(scriptingCommands).eval(
                    any(byte[].class),
                    org.mockito.ArgumentMatchers.eq(ReturnType.INTEGER),
                    org.mockito.ArgumentMatchers.eq(3),
                    keysAndArgsCaptor.capture()
            );
            assertThat(keysAndArgsCaptor.getValue()[4])
                    .isEqualTo(legacySerializer.serialize("old-token"));
            return result;
        });

        boolean rotated = tokenCacheService.compareAndRotateRefreshToken(
                "member-1",
                "session-1",
                "old-token",
                "new-token",
                Duration.ofMillis(1_234L)
        );

        assertThat(rotated).isTrue();
    }

    @Test
    void deleteRefreshTokenIfMatchesSendsLegacySerializedExpectedTokenToLua() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(1_234L);
        RedisSerializer<Object> legacySerializer = legacyValueSerializer();
        doReturn(legacySerializer).when(redisTemplate).getValueSerializer();

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisScriptingCommands scriptingCommands = mock(RedisScriptingCommands.class, evalInvocation -> {
                if ("eval".equals(evalInvocation.getMethod().getName())) {
                    return 1L;
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(evalInvocation);
            });
            when(connection.scriptingCommands()).thenReturn(scriptingCommands);

            Object result = callback.doInRedis(connection);

            ArgumentCaptor<byte[][]> keysAndArgsCaptor = ArgumentCaptor.forClass(byte[][].class);
            verify(scriptingCommands).eval(
                    any(byte[].class),
                    org.mockito.ArgumentMatchers.eq(ReturnType.INTEGER),
                    org.mockito.ArgumentMatchers.eq(3),
                    keysAndArgsCaptor.capture()
            );
            byte[][] keysAndArgs = keysAndArgsCaptor.getValue();
            assertThat(new String(keysAndArgs[0], StandardCharsets.UTF_8))
                    .isEqualTo("refreshToken::member-1::session-1");
            assertThat(new String(keysAndArgs[1], StandardCharsets.UTF_8))
                    .isEqualTo("refreshToken::member-1");
            assertThat(new String(keysAndArgs[2], StandardCharsets.UTF_8))
                    .isEqualTo("refreshTokenSessions::member-1");
            assertThat(keysAndArgs[3])
                    .isEqualTo("old-token".getBytes(StandardCharsets.UTF_8));
            assertThat(keysAndArgs[4])
                    .isEqualTo(legacySerializer.serialize("old-token"));
            return result;
        });

        boolean deleted = tokenCacheService.deleteRefreshTokenIfMatches(
                "member-1",
                "session-1",
                "old-token"
        );

        assertThat(deleted).isTrue();
    }

    @Test
    void getRefreshTokenDeserializesLegacySerializedValue() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(1_234L);
        RedisSerializer<Object> legacySerializer = legacyValueSerializer();
        doReturn(legacySerializer).when(redisTemplate).getValueSerializer();
        List<String> requestedKeys = new ArrayList<>();

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisStringCommands stringCommands = mock(RedisStringCommands.class, getInvocation -> {
                if ("get".equals(getInvocation.getMethod().getName())) {
                    String key = new String(getInvocation.getArgument(0), StandardCharsets.UTF_8);
                    requestedKeys.add(key);
                    return key.endsWith("::session-1")
                            ? null
                            : legacySerializer.serialize("old-token");
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(getInvocation);
            });
            when(connection.stringCommands()).thenReturn(stringCommands);

            return callback.doInRedis(connection);
        });

        String refreshToken = tokenCacheService.getRefreshToken("member-1", "session-1");

        assertThat(refreshToken).isEqualTo("old-token");
        assertThat(requestedKeys).containsExactly(
                "refreshToken::member-1::session-1",
                "refreshToken::member-1"
        );
    }

    @Test
    void saveRefreshTokenUsesSessionKeyAndMillisecondTtl() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(5_678L);

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisScriptingCommands scriptingCommands = mock(RedisScriptingCommands.class, evalInvocation -> {
                if ("eval".equals(evalInvocation.getMethod().getName())) {
                    return 1L;
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(evalInvocation);
            });
            when(connection.scriptingCommands()).thenReturn(scriptingCommands);

            Object result = callback.doInRedis(connection);

            ArgumentCaptor<byte[][]> keysAndArgsCaptor = ArgumentCaptor.forClass(byte[][].class);
            verify(scriptingCommands).eval(
                    any(byte[].class),
                    org.mockito.ArgumentMatchers.eq(ReturnType.INTEGER),
                    org.mockito.ArgumentMatchers.eq(2),
                    keysAndArgsCaptor.capture()
            );
            assertThat(Arrays.stream(keysAndArgsCaptor.getValue())
                    .map(value -> new String(value, StandardCharsets.UTF_8))
                    .toList())
                    .containsExactly(
                            "refreshToken::member-1::session-1",
                            "refreshTokenSessions::member-1",
                            "refresh-token",
                            "5678"
                    );
            return result;
        });

        tokenCacheService.saveRefreshToken("member-1", "session-1", "refresh-token");
    }

    @Test
    void deleteRefreshTokenDeletesSessionIndexAndLegacyKey() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(5_678L);

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisScriptingCommands scriptingCommands = mock(RedisScriptingCommands.class, evalInvocation -> {
                if ("eval".equals(evalInvocation.getMethod().getName())) {
                    return 2L;
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(evalInvocation);
            });
            when(connection.scriptingCommands()).thenReturn(scriptingCommands);

            Object result = callback.doInRedis(connection);

            ArgumentCaptor<byte[][]> keysCaptor = ArgumentCaptor.forClass(byte[][].class);
            verify(scriptingCommands).eval(
                    any(byte[].class),
                    org.mockito.ArgumentMatchers.eq(ReturnType.INTEGER),
                    org.mockito.ArgumentMatchers.eq(2),
                    keysCaptor.capture()
            );
            assertThat(Arrays.stream(keysCaptor.getValue())
                    .map(value -> new String(value, StandardCharsets.UTF_8))
                    .toList())
                    .containsExactly(
                            "refreshTokenSessions::member-1",
                            "refreshToken::member-1"
                    );
            return result;
        });

        tokenCacheService.deleteRefreshToken("member-1");
    }

    @Test
    void saveOAuthExchangeCodeUsesRawStringValueWithTwoMinuteTtl() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(5_678L);

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisStringCommands stringCommands = mock(RedisStringCommands.class, setInvocation -> {
                if ("set".equals(setInvocation.getMethod().getName())) {
                    return true;
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(setInvocation);
            });
            when(connection.stringCommands()).thenReturn(stringCommands);

            Object result = callback.doInRedis(connection);

            ArgumentCaptor<byte[]> keyCaptor = ArgumentCaptor.forClass(byte[].class);
            ArgumentCaptor<byte[]> valueCaptor = ArgumentCaptor.forClass(byte[].class);
            ArgumentCaptor<Expiration> expirationCaptor = ArgumentCaptor.forClass(Expiration.class);
            verify(stringCommands).set(
                    keyCaptor.capture(),
                    valueCaptor.capture(),
                    expirationCaptor.capture(),
                    org.mockito.ArgumentMatchers.eq(RedisStringCommands.SetOption.upsert())
            );
            assertSoftly(softly -> {
                softly.assertThat(new String(keyCaptor.getValue(), StandardCharsets.UTF_8))
                        .isEqualTo("oauthCode::exchange-code");
                softly.assertThat(new String(valueCaptor.getValue(), StandardCharsets.UTF_8))
                        .isEqualTo("true|refresh-token");
                softly.assertThat(expirationCaptor.getValue().getExpirationTimeInMilliseconds())
                        .isEqualTo(120_000L);
            });
            return result;
        });

        tokenCacheService.saveOAuthExchangeCode("exchange-code", "true|refresh-token");
    }

    @Test
    void consumeOAuthExchangeCodeUsesLuaAndReturnsRawValue() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(5_678L);

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisScriptingCommands scriptingCommands = mock(RedisScriptingCommands.class, evalInvocation -> {
                if ("eval".equals(evalInvocation.getMethod().getName())) {
                    return "true|refresh-token".getBytes(StandardCharsets.UTF_8);
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(evalInvocation);
            });
            when(connection.scriptingCommands()).thenReturn(scriptingCommands);

            Object result = callback.doInRedis(connection);

            ArgumentCaptor<byte[][]> keysAndArgsCaptor = ArgumentCaptor.forClass(byte[][].class);
            verify(scriptingCommands).eval(
                    any(byte[].class),
                    org.mockito.ArgumentMatchers.eq(ReturnType.VALUE),
                    org.mockito.ArgumentMatchers.eq(1),
                    keysAndArgsCaptor.capture()
            );
            assertSoftly(softly -> {
                softly.assertThat(new String(keysAndArgsCaptor.getValue()[0], StandardCharsets.UTF_8))
                        .isEqualTo("oauthCode::exchange-code");
                softly.assertThat(new String((byte[]) result, StandardCharsets.UTF_8))
                        .isEqualTo("true|refresh-token");
            });
            return result;
        });

        String value = tokenCacheService.consumeOAuthExchangeCode("exchange-code");

        assertThat(value).isEqualTo("true|refresh-token");
    }

    @Test
    void consumeOAuthExchangeCodeReturnsNullWhenCodeDoesNotExist() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(5_678L);

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisScriptingCommands scriptingCommands = mock(RedisScriptingCommands.class, evalInvocation -> {
                if ("eval".equals(evalInvocation.getMethod().getName())) {
                    return null;
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(evalInvocation);
            });
            when(connection.scriptingCommands()).thenReturn(scriptingCommands);

            return callback.doInRedis(connection);
        });

        String value = tokenCacheService.consumeOAuthExchangeCode("missing-code");

        assertThat(value).isNull();
    }

    private TokenCacheService tokenCacheServiceWithRefreshTtl(long refreshTtlMs) {
        JwtProperties jwtProperties = new JwtProperties();
        JwtProperties.TokenValidity tokenValidity = new JwtProperties.TokenValidity();
        tokenValidity.setRefreshToken(refreshTtlMs);
        jwtProperties.setTokenValidity(tokenValidity);
        return new TokenCacheService(redisTemplate, jwtProperties);
    }

    private RedisSerializer<Object> legacyValueSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
