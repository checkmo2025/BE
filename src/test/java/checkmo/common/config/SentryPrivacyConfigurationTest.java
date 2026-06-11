package checkmo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.support.SpringTest;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import io.sentry.protocol.User;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SpringTest
class SentryPrivacyConfigurationTest {

    @Autowired
    private SentryPrivacyPolicy privacyPolicy;

    @Autowired
    private SentryOptions.BeforeSendCallback beforeSendCallback;

    @Test
    void sendDefaultPiiIsDisabled() {
        assertThat(privacyPolicy.sendDefaultPii()).isFalse();
    }

    @Test
    void requestBodiesAreNotCaptured() {
        assertThat(privacyPolicy.captureRequestBody()).isFalse();
    }

    @Test
    void sensitiveHeadersAndCookiesAreNotSent() {
        assertThat(privacyPolicy.shouldSendHeader("Authorization")).isFalse();
        assertThat(privacyPolicy.shouldSendHeader("Cookie")).isFalse();
        assertThat(privacyPolicy.shouldSendHeader("X-Request-Id")).isTrue();
    }

    @Test
    void sensitiveQueryParametersAreNotSent() {
        assertThat(privacyPolicy.shouldSendQueryParameter("accessToken")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("refresh_token")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("password")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("verificationCode")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("page")).isTrue();
    }

    @Test
    void userContextIsNotAttachedByDefault() {
        assertThat(privacyPolicy.attachUserContext()).isFalse();
    }

    @Test
    void beforeSendRemovesSensitiveRequestDataAndUserContext() {
        SentryEvent event = new SentryEvent(new RuntimeException("unexpected"));
        Request request = new Request();
        request.setData("raw-body");
        request.setCookies("SESSION=secret");
        request.setQueryString("page=1&refreshToken=secret");
        request.setHeaders(Map.of(
                "Authorization", "Bearer secret",
                "Cookie", "SESSION=secret",
                "X-Request-Id", "request-id"
        ));
        event.setRequest(request);

        User user = new User();
        user.setEmail("user@example.com");
        event.setUser(user);

        SentryEvent sanitized = beforeSendCallback.execute(event, null);

        assertThat(sanitized.getUser()).isNull();
        assertThat(sanitized.getRequest().getData()).isNull();
        assertThat(sanitized.getRequest().getCookies()).isNull();
        assertThat(sanitized.getRequest().getQueryString()).isNull();
        assertThat(sanitized.getRequest().getHeaders())
                .containsEntry("X-Request-Id", "request-id")
                .doesNotContainKeys("Authorization", "Cookie");
    }

    @Test
    void beforeSendRemovesAllQueryStrings() {
        SentryEvent event = new SentryEvent(new RuntimeException("unexpected"));
        Request request = new Request();
        request.setQueryString("page=1&message=secret-refresh-token&email=user@example.com");
        event.setRequest(request);

        SentryEvent sanitized = beforeSendCallback.execute(event, null);

        assertThat(sanitized.getRequest().getQueryString()).isNull();
    }
}
