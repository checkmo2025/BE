package checkmo.domain.member.service.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @CachePut(value = "refreshToken", key = "#memberId")
    public String saveRefreshToken(String memberId, String refreshToken) {
        log.info("리프레시 토큰 저장 - memberId={}", memberId);
        return refreshToken;
    }

    // 이건 Redis에서 직접 조회하는 메서드로, @Cacheable을 사용하지 않고 RedisTemplate을 통해 조회
    public String getRefreshToken(String memberId) {
        log.info("리프레시 토큰 조회 - memberId={}", memberId);
        return (String) redisTemplate.opsForValue().get("refreshToken::" + memberId);
    }

    // Redis에서 리프레시 토큰 삭제
    @CacheEvict(value = "refreshToken", key = "#memberId")
    public void deleteRefreshToken(String memberId) {
        log.info("리프레시 토큰 삭제 - memberId={}", memberId);
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
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist::" + accessToken));
    }
}
