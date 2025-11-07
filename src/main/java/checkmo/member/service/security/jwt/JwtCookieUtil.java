package checkmo.member.service.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieUtil {

    public void addTokenToCookie(HttpServletResponse response, String cookieName, String token, int maxAge) {
/*
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true); // 클라이언트 스크립트에서 접근 불가
        cookie.setAttribute("SameSite", "None"); // 프론트 배포 성공 시 사용, TODO : 프론트 배포 성공 시 주석해제
        //cookie.setAttribute("SameSite", "Lax"); // 프론트가 개발중일 때 사용, TODO : 프론트 배포 성공 시 삭제
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);
        cookie.setSecure(true); // HTTPS에서만 전송하도록 추가
        response.addCookie(cookie);
*/
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public String resolveToken(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null; // 쿠키에서 Access Token을 찾지 못한 경우
    }

    public void deleteTokenFromCookie(HttpServletResponse response, String cookieName) {
/*
        // 쿠키에서 토큰을 삭제
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "None"); // 프론트 배포 성공 시 사용, TODO : 프론트 배포 성공 시 주석해제
        cookie.setSecure(true); // 프론트가 개발중일 때 사용, TODO : 프론트 배포 성공 시 true로 변경
        response.addCookie(cookie);
*/

        ResponseCookie cookie = ResponseCookie.from(cookieName, null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
