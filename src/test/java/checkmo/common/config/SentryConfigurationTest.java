package checkmo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.common.monitoring.SentryCaptureClient;
import checkmo.common.monitoring.SentrySdkCaptureClient;
import checkmo.support.SpringTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

@SpringTest
class SentryConfigurationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private SentryMonitoringProperties properties;

    @Autowired
    private SentryCaptureClient captureClient;

    @Autowired
    private ListableBeanFactory beanFactory;

    @Test
    void contextStartsWhenSentryDsnIsMissing() {
        assertThat(environment.getProperty("sentry.dsn")).isNull();
        assertThat(environment.getProperty("checkmo.sentry.dsn", "")).isBlank();
    }

    @Test
    void sentryIsDisabledInTestProfile() {
        assertThat(properties.enabled()).isFalse();
        assertThat(environment.getProperty("checkmo.sentry.enabled")).isEqualTo("false");
    }

    @Test
    void sentryDisabledUsesNoOpCaptureClient() {
        assertThat(captureClient).isNotInstanceOf(SentrySdkCaptureClient.class);
    }

    @Test
    void sentryRuntimePropertiesBindFromEnvironmentNames() {
        assertThat(properties.dsn()).isBlank();
        assertThat(properties.environment()).isEqualTo("test");
        assertThat(properties.release()).isEqualTo("local-test");
    }

    @Test
    void sentryStarterDsnIsNotConfiguredByCommittedProperties() {
        assertThat(environment.getProperty("sentry.dsn")).isNull();
    }

    @Test
    void sentryStarterAutoConfigurationIsExcluded() {
        List<String> excludes = Binder.get(environment)
                .bind("spring.autoconfigure.exclude", Bindable.listOf(String.class))
                .orElse(List.of());

        assertThat(excludes)
                .contains(
                        "io.sentry.spring.boot.jakarta.SentryAutoConfiguration",
                        "io.sentry.spring.boot.jakarta.SentryProfilerAutoConfiguration",
                        "io.sentry.spring.boot.jakarta.SentryLogbackAppenderAutoConfiguration",
                        "io.sentry.spring.boot.jakarta.SentryWebfluxAutoConfiguration"
                );
        assertThat(beanFactory.containsBean("sentryHub")).isFalse();
    }

    @Test
    void tracingIsNotEnabledByCommittedProperties() {
        assertThat(environment.getProperty("sentry.traces-sample-rate")).isNull();
    }

    @Test
    void sentryDisabledWithDsnStillUsesNoOpCaptureClient() {
        SentryCaptureClient client = new SentryMonitoringConfiguration().sentryCaptureClient(
                new SentryMonitoringProperties(false, "https://public@example.com/1", "prod", "release"),
                new SentrySanitizingBeforeSendCallback(new SentryPrivacyPolicy())
        );

        assertThat(client).isNotInstanceOf(SentrySdkCaptureClient.class);
    }

    @Test
    void sentryEnabledWithBlankDsnUsesNoOpCaptureClient() {
        SentryCaptureClient client = new SentryMonitoringConfiguration().sentryCaptureClient(
                new SentryMonitoringProperties(true, "", "prod", "release"),
                new SentrySanitizingBeforeSendCallback(new SentryPrivacyPolicy())
        );

        assertThat(client).isNotInstanceOf(SentrySdkCaptureClient.class);
    }
}
