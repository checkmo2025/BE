package checkmo.common.config;

import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SentryPrivacyPolicy {

    private static final Set<String> BLOCKED_HEADERS = Set.of(
            "authorization",
            "cookie",
            "set-cookie",
            "x-access-token",
            "x-refresh-token"
    );
    private static final Set<String> BLOCKED_QUERY_KEYWORDS = Set.of(
            "authorization",
            "cookie",
            "jwt",
            "token",
            "refresh",
            "password",
            "verification",
            "code"
    );

    public boolean sendDefaultPii() {
        return false;
    }

    public boolean captureRequestBody() {
        return false;
    }

    public boolean attachUserContext() {
        return false;
    }

    public boolean shouldSendHeader(String headerName) {
        if (headerName == null || headerName.isBlank()) {
            return false;
        }
        return !BLOCKED_HEADERS.contains(headerName.toLowerCase(Locale.ROOT));
    }

    public boolean shouldSendQueryParameter(String parameterName) {
        if (parameterName == null || parameterName.isBlank()) {
            return false;
        }
        String normalized = parameterName.toLowerCase(Locale.ROOT);
        return BLOCKED_QUERY_KEYWORDS.stream().noneMatch(normalized::contains);
    }
}
