package checkmo.domain.member.service.security.jwt;

import checkmo.common.config.properties.JwtProperties;
import checkmo.domain.member.service.security.auth.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private final Key key;
    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtTokenProviderImpl(JwtProperties jwtProperties,
                                CustomUserDetailsService customUserDetailsService) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtProperties = jwtProperties;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public JwtToken generateToken(Authentication authentication) {

        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                                           .map(GrantedAuthority::getAuthority)
                                           .collect(Collectors.joining(","));

        // 현재 시간
        long now = (new Date()).getTime();

        // 액세스 토큰과 리프레시 토큰 유효 시간 가져오기
        long accessTokenValidity = jwtProperties.getTokenValidity().getAccessToken();
        long refreshTokenValidity = jwtProperties.getTokenValidity().getRefreshToken();

        // 액세스 토큰 생성
        String accessToken = Jwts.builder()
                                 .subject(authentication.getName())
                                 .claim("role", authorities)
                                 .expiration(new Date(now + accessTokenValidity))
                                 .signWith(key)
                                 .compact();

        // 리프레시 토큰 생성
        String refreshToken = Jwts.builder()
                                  .subject(authentication.getName())
                                  .expiration(new Date(now + refreshTokenValidity))
                                  .signWith(key)
                                  .compact();

        return JwtToken.builder()
                       .accessToken(accessToken)
                       .refreshToken(refreshToken)
                       .build();
    }

    @Override
    public Authentication getAuthentication(String accessToken) {
        String userId = this.getUserIdFromToken(accessToken);

        UserDetails userDetails = customUserDetailsService.loadUserById(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, "",
            userDetails.getAuthorities());
    }

    @Override
    public boolean validateToken(String token) {

        if (!StringUtils.hasText(token)) {
            log.warn("JWT 토큰이 null 입니다.");
            return false;
        }

        try {
            Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e; // 토큰이 만료된 경우 재발급하도록 던지기
        }
        catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다", e);
        }
        return false;
    }

    @Override
    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(refreshToken);
            return true;
        } catch (Exception e) {
            log.warn("유효하지 않은 Refresh Token 입니다: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUserIdFromToken(String token) {
        try {
            return Jwts.parser()
                       .verifyWith((SecretKey) key)
                       .build()
                       .parseSignedClaims(token)
                       .getPayload()
                       .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    @Override
    public Authentication getAuthenticationFromMemberId(String memberId) {
        UserDetails userDetails = customUserDetailsService.loadUserById(memberId);

        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
    }

    @Override
    public long getAccessTokenExpirationTime() {
        return jwtProperties.getTokenValidity().getAccessToken();
    }

    @Override
    public long getRefreshTokenExpirationTime() {
        return jwtProperties.getTokenValidity().getRefreshToken();
    }
}
