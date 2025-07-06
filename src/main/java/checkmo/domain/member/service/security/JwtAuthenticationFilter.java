package checkmo.domain.member.service.security;

/**
 * JWT 토큰 기반 인증 필터
 *
 * 모든 HTTP 요청에 대해 쿠키에서 JWT 토큰을 추출하고 토큰 검증
 * 액세스 토큰 만료시 리프레시 토큰으로 자동 갱신
 * 토큰 검증 성공시 SecurityContext에 인증 정보 설정
 * JwtTokenProvider 사용해서 토큰 검증 로직 처리
 * -> JwtAuthenticationFilter에서는 토큰을 직접 검증하지 않음. JwtTokenProvider에서 토큰 검증 로직 구현
 */
public class JwtAuthenticationFilter /*extends OncePerRequestFilter*/ {
}
