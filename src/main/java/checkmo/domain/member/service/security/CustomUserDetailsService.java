package checkmo.domain.member.service.security;

/**
 * Spring Security UserDetailsService 구현체
 *
 * 일반 로그인 시 id로 회원을 조회하여 UserDetails 객체로 변환
 * 비활성화된 계정, 탈퇴한 계정 등의 상태 검증도 여기서 처리
 * loadUserByUsername 메소드 단순하게 오버라이딩 하면 됨
 */
public class CustomUserDetailsService /*implements UserDetailsService*/ {
}
