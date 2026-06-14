package checkmo.authentication.internal.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.Arrays;
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
                    org.mockito.ArgumentMatchers.eq(1),
                    keysAndArgsCaptor.capture()
            );
            byte[][] keysAndArgs = keysAndArgsCaptor.getValue();
            assertThat(Arrays.stream(keysAndArgs)
                    .map(value -> new String(value, StandardCharsets.UTF_8))
                    .toList())
                    .containsExactly(
                            "refreshToken::member-1",
                            "old-token",
                            new String(legacySerializer.serialize("old-token"), StandardCharsets.UTF_8),
                            "new-token",
                            "1234"
                    );
            return result;
        });

        boolean rotated = tokenCacheService.compareAndRotateRefreshToken(
                "member-1",
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
                    org.mockito.ArgumentMatchers.eq(1),
                    keysAndArgsCaptor.capture()
            );
            assertThat(keysAndArgsCaptor.getValue()[2])
                    .isEqualTo(legacySerializer.serialize("old-token"));
            return result;
        });

        boolean rotated = tokenCacheService.compareAndRotateRefreshToken(
                "member-1",
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
                    org.mockito.ArgumentMatchers.eq(1),
                    keysAndArgsCaptor.capture()
            );
            byte[][] keysAndArgs = keysAndArgsCaptor.getValue();
            assertThat(new String(keysAndArgs[0], StandardCharsets.UTF_8))
                    .isEqualTo("refreshToken::member-1");
            assertThat(keysAndArgs[1])
                    .isEqualTo("old-token".getBytes(StandardCharsets.UTF_8));
            assertThat(keysAndArgs[2])
                    .isEqualTo(legacySerializer.serialize("old-token"));
            return result;
        });

        boolean deleted = tokenCacheService.deleteRefreshTokenIfMatches("member-1", "old-token");

        assertThat(deleted).isTrue();
    }

    @Test
    void getRefreshTokenDeserializesLegacySerializedValue() {
        tokenCacheService = tokenCacheServiceWithRefreshTtl(1_234L);
        RedisSerializer<Object> legacySerializer = legacyValueSerializer();
        doReturn(legacySerializer).when(redisTemplate).getValueSerializer();

        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            RedisConnection connection = mock(RedisConnection.class);
            RedisStringCommands stringCommands = mock(RedisStringCommands.class, getInvocation -> {
                if ("get".equals(getInvocation.getMethod().getName())) {
                    return legacySerializer.serialize("old-token");
                }
                return org.mockito.Mockito.RETURNS_DEFAULTS.answer(getInvocation);
            });
            when(connection.stringCommands()).thenReturn(stringCommands);

            return callback.doInRedis(connection);
        });

        String refreshToken = tokenCacheService.getRefreshToken("member-1");

        assertThat(refreshToken).isEqualTo("old-token");
    }

    @Test
    void saveRefreshTokenUsesRawStringValueWithMillisecondTtl() {
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
            assertThat(new String(keyCaptor.getValue(), StandardCharsets.UTF_8))
                    .isEqualTo("refreshToken::member-1");
            assertThat(new String(valueCaptor.getValue(), StandardCharsets.UTF_8))
                    .isEqualTo("refresh-token");
            assertThat(expirationCaptor.getValue().getExpirationTimeInMilliseconds())
                    .isEqualTo(5_678L);
            return result;
        });

        tokenCacheService.saveRefreshToken("member-1", "refresh-token");
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
