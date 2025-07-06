package checkmo.domain.member.service.security;

/**
 * 소셜 로그인 성공 후의 성공 처리 핸들러
 *
 * CustomOAuth2UserService에서 인증 성공 후 이 Success Handler로 요청이 자동으로 넘어와서
 * JWT 토큰 생성, 쿠키 설정등등 작업 수행
 */
public class OAuth2AuthenticationSuccessHandler /*extends SimpleUrlAuthenticationSuccessHandler*/ {
}
