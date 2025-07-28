package checkmo.domain.member.service.security.jwt;

import checkmo.config.properties.JwtProperties;
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
        long accessTokenValidity = jwtProperties.getTokenValidity().getAccessToken() * 1000;
        long refreshTokenValidity = jwtProperties.getTokenValidity().getRefreshToken() * 1000;

        // 액세스 토큰 생성
        String accessToken = Jwts.builder()
                                 .subject(authentication.getName())
                                 .claim("role", authorities)
                                 .expiration(new Date(now + accessTokenValidity))
                                 .signWith(key)
                                 .compact();

        // 리프레시 토큰 생성
        String refreshToken = Jwts.builder()
                                  .expiration(new Date(now + refreshTokenValidity))
                                  .signWith(key)
                                  .compact();

        return JwtToken.builder()
                       .grantType("Bearer")
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
        try {
            // TODO: 로그아웃 시 토큰 블랙리스트 검증 로직 추가
            Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 서명입니다", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다", e);
        }
        return false;
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
}
