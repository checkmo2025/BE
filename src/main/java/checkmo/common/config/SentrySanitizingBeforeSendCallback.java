package checkmo.common.config;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import io.sentry.protocol.SentryException;
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
        sanitizeExceptions(event);
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
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> privacyPolicy.shouldSendHeader(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> right)));
    }

    private void sanitizeQueryString(Request request) {
        request.setQueryString(null);
        stripQueryFromUrl(request);
    }

    private void stripQueryFromUrl(Request request) {
        String url = request.getUrl();
        if (url == null) {
            return;
        }
        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return;
        }
        int fragmentStart = url.indexOf('#', queryStart);
        if (fragmentStart < 0) {
            request.setUrl(url.substring(0, queryStart));
            return;
        }
        request.setUrl(url.substring(0, queryStart) + url.substring(fragmentStart));
    }

    private void sanitizeExceptions(SentryEvent event) {
        if (event.getExceptions() == null) {
            return;
        }
        for (SentryException sentryException : event.getExceptions()) {
            sentryException.setValue("Unexpected backend exception");
        }
    }
}
