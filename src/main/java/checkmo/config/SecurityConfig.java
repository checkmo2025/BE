package checkmo.config;

import checkmo.domain.member.service.security.auth.ProfileCompletionAuthorizationFilter;
import checkmo.domain.member.service.security.jwt.JwtAuthenticationFilter;
import checkmo.domain.member.service.security.oauth2.CustomOAuth2UserService;
import checkmo.domain.member.service.security.oauth2.OAuth2AuthenticationFailureHandler;
import checkmo.domain.member.service.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ProfileCompletionAuthorizationFilter profileCompletionAuthorizationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
        throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안함
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/").permitAll() // Swagger UI 접근 허용
                .requestMatchers("/login/oauth2").permitAll() // OAuth2 로그인 허용
                .requestMatchers("/api/auth/additional-info").authenticated()
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401); // 로그인 안한 사용자 → 리다이렉트 없이 401 응답
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\": \"UNAUTHORIZED\", \"message\": \"로그인이 필요합니다.\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class) // JWT 인증 필터 추가
            .addFilterAfter(profileCompletionAuthorizationFilter,
                JwtAuthenticationFilter.class); // 프로필 완료 필터 추가

        // OAuth2 로그인 설정
        http
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler) // 로그인 성공 핸들러 설정
                .failureHandler(oAuth2AuthenticationFailureHandler) // 로그인 실패 핸들러 설정
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
