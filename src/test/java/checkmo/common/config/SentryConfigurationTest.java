package checkmo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.common.monitoring.SentryCaptureClient;
import checkmo.common.monitoring.SentrySdkCaptureClient;
import checkmo.support.SpringTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        assertThat(environment.getProperty("spring.autoconfigure.exclude[0]"))
                .isEqualTo("io.sentry.spring.boot.jakarta.SentryAutoConfiguration");
        assertThat(environment.getProperty("spring.autoconfigure.exclude[1]"))
                .isEqualTo("io.sentry.spring.boot.jakarta.SentryProfilerAutoConfiguration");
        assertThat(environment.getProperty("spring.autoconfigure.exclude[2]"))
                .isEqualTo("io.sentry.spring.boot.jakarta.SentryLogbackAppenderAutoConfiguration");
        assertThat(environment.getProperty("spring.autoconfigure.exclude[3]"))
                .isEqualTo("io.sentry.spring.boot.jakarta.SentryWebfluxAutoConfiguration");
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
