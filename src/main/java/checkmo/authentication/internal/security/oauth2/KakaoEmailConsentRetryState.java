package checkmo.authentication.internal.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

final class KakaoEmailConsentRetryState {

    static final String SESSION_ATTRIBUTE = "checkmo.oauth2.kakao.email-consent-retry";

    private KakaoEmailConsentRetryState() {
    }

    static boolean beginRetry(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        if (Boolean.TRUE.equals(session.getAttribute(SESSION_ATTRIBUTE))) {
            session.removeAttribute(SESSION_ATTRIBUTE);
            return false;
        }

        session.setAttribute(SESSION_ATTRIBUTE, Boolean.TRUE);
        return true;
    }

    static void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_ATTRIBUTE);
        }
    }
}
