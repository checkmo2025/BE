package checkmo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.book.internal.service.AladinRecommendationRefreshClient;
import checkmo.support.SpringTest;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringTest
@TestPropertySource(properties = "aladin.api.recommendation.refresh.background.fixed-delay=5m")
class SentryPrivacyConfigurationTest {

    @Autowired
    private SentryPrivacyPolicy privacyPolicy;

    @Autowired
    private SentryOptions.BeforeSendCallback beforeSendCallback;

    @MockitoBean
    private AladinRecommendationRefreshClient aladinRecommendationRefreshClient;

    @Test
    void sentrySanitizerIsRegisteredAsBeforeSendCallbackBean() {
        assertThat(beforeSendCallback).isInstanceOf(SentrySanitizingBeforeSendCallback.class);
    }

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
        assertThat(privacyPolicy.shouldSendHeader("AUTHORIZATION")).isFalse();
        assertThat(privacyPolicy.shouldSendHeader("Cookie")).isFalse();
        assertThat(privacyPolicy.shouldSendHeader("RefreshToken")).isFalse();
        assertThat(privacyPolicy.shouldSendHeader("X-Refresh-Token")).isFalse();
        assertThat(privacyPolicy.shouldSendHeader("X-JWT")).isFalse();
        assertThat(privacyPolicy.shouldSendHeader("Verification-Code")).isFalse();
        assertThat(privacyPolicy.shouldSendHeader("X-Request-Id")).isTrue();
        assertThat(privacyPolicy.shouldSendHeader("X-Status-Code")).isTrue();
        assertThat(privacyPolicy.shouldSendHeader("X-Error-Code")).isTrue();
    }

    @Test
    void sensitiveQueryParametersAreNotSent() {
        assertThat(privacyPolicy.shouldSendQueryParameter("accessToken")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("refresh_token")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("RefreshToken")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("password")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("verificationCode")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("verification-code")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("jwt")).isFalse();
        assertThat(privacyPolicy.shouldSendQueryParameter("page")).isTrue();
        assertThat(privacyPolicy.shouldSendQueryParameter("categoryCode")).isTrue();
        assertThat(privacyPolicy.shouldSendQueryParameter("productCode")).isTrue();
    }

    @Test
    void userContextIsNotAttachedByDefault() {
        assertThat(privacyPolicy.attachUserContext()).isFalse();
    }

    @Test
    void beforeSendRemovesSensitiveRequestDataAndUserContext() {
        SentryEvent event = new SentryEvent(new RuntimeException("unexpected"));
        Request request = new Request();
        request.setData("password=secret&verification-code=123456");
        request.setCookies("SESSION=secret");
        request.setQueryString("page=1&refreshToken=secret&jwt=secret");
        request.setUrl("https://api.checkmo.kr/books?refreshToken=secret&page=1&jwt=secret");
        request.setHeaders(Map.of(
                "Authorization", "Bearer secret",
                "RefreshToken", "secret",
                "X-JWT", "secret",
                "Verification-Code", "123456",
                "Cookie", "SESSION=secret",
                "X-Request-Id", "request-id",
                "X-Status-Code", "500"
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
        assertThat(sanitized.getRequest().getUrl()).isEqualTo("https://api.checkmo.kr/books");
        assertThat(sanitized.getRequest().getHeaders())
                .containsEntry("X-Request-Id", "request-id")
                .containsEntry("X-Status-Code", "500")
                .doesNotContainKeys("Authorization", "RefreshToken", "X-JWT", "Verification-Code", "Cookie");
    }

    @Test
    void beforeSendRemovesAllQueryStrings() {
        SentryEvent event = new SentryEvent(new RuntimeException("unexpected"));
        Request request = new Request();
        request.setQueryString("page=1&message=secret-refresh-token&email=user@example.com");
        request.setUrl("https://api.checkmo.kr/search?page=1&message=secret-refresh-token#result");
        event.setRequest(request);

        SentryEvent sanitized = beforeSendCallback.execute(event, null);

        assertThat(sanitized.getRequest().getQueryString()).isNull();
        assertThat(sanitized.getRequest().getUrl()).isEqualTo("https://api.checkmo.kr/search#result");
    }

    @Test
    void beforeSendRemovesBookSearchKeywordAndQueryString() {
        SentryEvent event = new SentryEvent(new RuntimeException("aladin unavailable"));
        Request request = new Request();
        request.setQueryString("keyword=자바&page=1&refreshToken=secret");
        request.setUrl("https://api.checkmo.kr/api/v1/books/search?keyword=%EC%9E%90%EB%B0%94&page=1&refreshToken=secret");
        event.setRequest(request);

        SentryEvent sanitized = beforeSendCallback.execute(event, null);

        assertThat(sanitized.getRequest().getQueryString()).isNull();
        assertThat(sanitized.getRequest().getUrl()).isEqualTo("https://api.checkmo.kr/api/v1/books/search");
    }

    @Test
    void beforeSendIgnoresHeadersWithNullNamesOrValues() {
        SentryEvent event = new SentryEvent(new RuntimeException("unexpected"));
        Request request = new Request();
        Map<String, String> headers = new HashMap<>();
        headers.put(null, "anonymous");
        headers.put("X-Nullable", null);
        headers.put("X-Request-Id", "request-id");
        request.setHeaders(headers);
        event.setRequest(request);

        SentryEvent sanitized = beforeSendCallback.execute(event, null);

        assertThat(sanitized.getRequest().getHeaders())
                .containsEntry("X-Request-Id", "request-id")
                .doesNotContainKeys("X-Nullable");
    }

    @Test
    void beforeSendRemovesExceptionMessagesWithoutChangingExceptionType() {
        SentryEvent event = new SentryEvent();
        SentryException exception = new SentryException();
        exception.setType("java.lang.IllegalStateException");
        exception.setValue("refreshToken=secret password=secret verificationCode=123456");
        event.setExceptions(List.of(exception));

        SentryEvent sanitized = beforeSendCallback.execute(event, null);

        assertThat(sanitized.getExceptions()).singleElement()
                .satisfies(sanitizedException -> {
                    assertThat(sanitizedException.getType()).isEqualTo("java.lang.IllegalStateException");
                    assertThat(sanitizedException.getValue()).isEqualTo("Unexpected backend exception");
                });
    }
}
