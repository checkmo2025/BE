package checkmo.common.config;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SentrySanitizingBeforeSendCallback implements SentryOptions.BeforeSendCallback {

    private final SentryPrivacyPolicy privacyPolicy;

    public SentrySanitizingBeforeSendCallback(SentryPrivacyPolicy privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    @Override
    public SentryEvent execute(SentryEvent event, Hint hint) {
        if (event == null) {
            return null;
        }
        event.setUser(null);
        sanitizeRequest(event.getRequest());
        return event;
    }

    private void sanitizeRequest(Request request) {
        if (request == null) {
            return;
        }
        request.setData(null);
        request.setCookies(null);
        sanitizeHeaders(request);
        sanitizeQueryString(request);
    }

    private void sanitizeHeaders(Request request) {
        Map<String, String> headers = request.getHeaders();
        if (headers == null) {
            return;
        }
        request.setHeaders(headers.entrySet().stream()
                .filter(entry -> privacyPolicy.shouldSendHeader(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> right)));
    }

    private void sanitizeQueryString(Request request) {
        request.setQueryString(null);
    }
}
