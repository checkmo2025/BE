package checkmo.authentication.internal.config;

import checkmo.authentication.internal.security.auth.ProfileCompletionAuthorizationFilter;
import checkmo.authentication.internal.security.jwt.JwtAuthenticationFilter;
import checkmo.authentication.internal.security.apple.AppleClientSecretGenerator;
import checkmo.authentication.internal.security.oauth2.AppleClientSecretTokenRequestParametersConverter;
import checkmo.authentication.internal.security.oauth2.AppleOidcUserService;
import checkmo.authentication.internal.security.oauth2.AppleOAuth2AuthorizationRequestResolver;
import checkmo.authentication.internal.security.oauth2.OAuth2AuthenticationFailureHandler;
import checkmo.authentication.internal.security.oauth2.OAuth2AuthenticationSuccessHandler;
import checkmo.authentication.internal.security.oauth2.SocialOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ProfileCompletionAuthorizationFilter profileCompletionAuthorizationFilter;
    private final SocialOAuth2UserService socialOAuth2UserService;
    private final AppleOidcUserService appleOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안함
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll() // 홈페이지 접근 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/health").permitAll() // Swagger UI 접근 허용
                        .requestMatchers("/oauth2/authorization/**","/login/oauth2/**").permitAll() // OAuth2 로그인 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/me/likes").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/book-stories/me", "/api/v1/book-stories/following", "/api/v1/book-stories/clubs/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/book-stories", "/api/v1/book-stories/sitemap", "/api/v1/book-stories/*", "/api/v1/book-stories/search/*", "/api/v1/book-stories/members/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/news/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/news/sitemap", "/api/v1/news/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/app/version").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/terms").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/me", "/api/v1/members/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/members/find-email").permitAll()
                        .requestMatchers("/api/v1/members/additional-info").authenticated()
                        .requestMatchers("/api/v1/auth/**", "/api/v1/members/check-nickname").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/clubs", "/api/v1/clubs/sitemap", "/api/v1/clubs/*/home", "/api/v1/clubs/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/clubs/*/notices/latest").permitAll()
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
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(
                                        new AppleOAuth2AuthorizationRequestResolver(clientRegistrationRepository)
                                )
                        )
                        .tokenEndpoint(token -> token
                                .accessTokenResponseClient(accessTokenResponseClient)
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(socialOAuth2UserService)
                                .oidcUserService(appleOidcUserService)
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
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient(
            AppleClientSecretGenerator appleClientSecretGenerator
    ) {
        RestClientAuthorizationCodeTokenResponseClient client =
                new RestClientAuthorizationCodeTokenResponseClient();
        client.setParametersConverter(
                new AppleClientSecretTokenRequestParametersConverter(appleClientSecretGenerator)
        );
        return client;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
