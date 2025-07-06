package checkmo.domain.member.service.security;

/**
 * JWT 토큰 생성, 검증 서비스
 *
 * 액세스 토큰과 리프레시 토큰의 생성, 검증, 정보 추출을 담당
 * 토큰 만료 확인, 토큰에서 사용자 정보 추출 등 순수 토큰 로직만 처리
 * 쿠키 설정이나 SecurityContext 처리는 JwtAuthenticationFilter에서 담당
 */
public interface JwtTokenProvider {
}
