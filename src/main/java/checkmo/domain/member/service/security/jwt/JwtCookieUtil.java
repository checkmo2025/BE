package checkmo.domain.member.service.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieUtil {

    public void addTokenToCookie(HttpServletResponse response, String cookieName, String token, int maxAge) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true); // 클라이언트 스크립트에서 접근 불가
        cookie.setAttribute("SameSite", "Strict"); // CSRF 공격 방지
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
        // TODO: 배포 시 cookie.setSecure(true); // HTTPS에서만 전송하도록 추가
    }
}
