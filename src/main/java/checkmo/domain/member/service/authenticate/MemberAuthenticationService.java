package checkmo.domain.member.service.authenticate;

/**
 * 로그인/로그아웃 로직
 *
 * JWT 기반 로그인 담당하는 서비스
 * 사용자가 제출한 로그인 정보를 검증하고 JWT 토큰 생성/쿠키 설정/SecurityContext 저장까지의 전체 과정 관리
 * 로그아웃 시 토큰 무효화 및 쿠키 삭제, 리프레시 토큰을 통한 액세스 토큰 갱신 로직 포함
 */
public interface MemberAuthenticationService {
}
