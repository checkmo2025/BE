package checkmo.authentication.internal.security.jwt;

import checkmo.authentication.internal.config.properties.JwtProperties;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken::";
    private static final String BLACKLIST_PREFIX = "blacklist::";

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;

    public void saveRefreshToken(String userId, String refreshToken) {
        log.info("리프레시 토큰 저장 - userId={}", userId);
        long ttlMs = jwtProperties.getTokenValidity().getRefreshToken();
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                ttlMs,
                TimeUnit.MILLISECONDS
        );
    }

    public String getRefreshToken(String userId) {
        log.info("리프레시 토큰 조회 - userId={}", userId);
        return (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    public void deleteRefreshToken(String userId) {
        log.info("리프레시 토큰 삭제 - userId={}", userId);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
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
