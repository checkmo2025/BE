package checkmo.domain.member.service.authenticate;

import checkmo.domain.member.service.security.jwt.JwtToken;
import checkmo.domain.member.service.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAuthenticationServiceImpl implements MemberAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final HttpServletResponse response;

    @Override
    public void login(String email, String password) {

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(email, password);

        // 인증 요청
        Authentication authentication =
            authenticationManager.authenticate(authenticationToken);

        /// 인증 성공 후 SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        addTokenToCookie("accessToken", jwtToken.getAccessToken());
        addTokenToCookie("refreshToken", jwtToken.getRefreshToken());

        // TODO: Redis에 리프레시 토큰 저장 로직 추가
    }

    @Override
    public void logout(String token) {
        // TODO: 로그아웃 로직 구현
    }

    @Override
    public void reactivateMember() {
        // TODO: 계정 복구 로직 구현
    }

    private void addTokenToCookie(String cookieName, String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true); // 클라이언트 스크립트에서 접근 불가
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        response.addCookie(cookie);
    }
}
