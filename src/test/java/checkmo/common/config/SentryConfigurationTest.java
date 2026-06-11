package checkmo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.common.monitoring.SentryCaptureClient;
import checkmo.common.monitoring.SentrySdkCaptureClient;
import checkmo.support.SpringTest;
import io.sentry.SamplingContext;
import io.sentry.SentryOptions;
import io.sentry.TransactionContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

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
        assertThat(properties.tracesSampleRate()).isZero();
    }

    @Test
    void sentryRuntimePropertiesBindTracingSampleRateFromEnvironmentNames() {
        MockEnvironment mockEnvironment = new MockEnvironment()
                .withProperty("checkmo.sentry.traces-sample-rate", "0.1");

        SentryMonitoringProperties bound = Binder.get(mockEnvironment)
                .bind("checkmo.sentry", SentryMonitoringProperties.class)
                .orElseThrow(() -> new AssertionError("checkmo.sentry properties should bind"));

        assertThat(bound.tracesSampleRate()).isEqualTo(0.1);
    }

    @Test
    void sentryStarterDsnIsNotConfiguredByCommittedProperties() {
        assertThat(environment.getProperty("sentry.dsn")).isNull();
    }

    @Test
    void sentryStarterAutoConfigurationKeepsWebTracingAvailableWithoutProfilerLogbackOrWebflux() {
        List<String> excludes = Binder.get(environment)
                .bind("spring.autoconfigure.exclude", Bindable.listOf(String.class))
                .orElse(List.of());

        assertThat(excludes)
                .doesNotContain("io.sentry.spring.boot.jakarta.SentryAutoConfiguration")
                .contains(
                        "io.sentry.spring.boot.jakarta.SentryProfilerAutoConfiguration",
                        "io.sentry.spring.boot.jakarta.SentryLogbackAppenderAutoConfiguration",
                        "io.sentry.spring.boot.jakarta.SentryWebfluxAutoConfiguration"
                );
        assertThat(beanFactory.containsBean("sentryHub")).isFalse();
    }

    @Test
    void tracingSamplerDropsDisabledAndNoiseTransactions() {
        SentryMonitoringConfiguration configuration = new SentryMonitoringConfiguration();

        SentryOptions.TracesSamplerCallback disabledSampler = configuration.sentryTracesSamplerCallback(
                new SentryMonitoringProperties(false, "https://public@example.com/1", "prod", "release", 0.1)
        );
        SentryOptions.TracesSamplerCallback enabledSampler = configuration.sentryTracesSamplerCallback(
                new SentryMonitoringProperties(true, "https://public@example.com/1", "prod", "release", 0.1)
        );

        assertThat(disabledSampler.sample(samplingContext("GET /api/books"))).isZero();
        assertThat(enabledSampler.sample(samplingContext("GET /health"))).isZero();
        assertThat(enabledSampler.sample(samplingContext("GET /swagger-ui/index.html"))).isZero();
        assertThat(enabledSampler.sample(samplingContext("GET /v3/api-docs"))).isZero();
        assertThat(enabledSampler.sample(samplingContext("GET /api/v1/user/health"))).isEqualTo(0.1);
        assertThat(enabledSampler.sample(samplingContext("GET /api/books"))).isEqualTo(0.1);
    }

    @Test
    void tracingDefaultsToZeroByCommittedProperties() {
        assertThat(environment.getProperty("sentry.traces-sample-rate")).isEqualTo("0.0");
    }

    @Test
    void sentryDisabledWithDsnStillUsesNoOpCaptureClient() {
        SentryCaptureClient client = new SentryMonitoringConfiguration().sentryCaptureClient(
                new SentryMonitoringProperties(false, "https://public@example.com/1", "prod", "release", 0.1)
        );

        assertThat(client).isNotInstanceOf(SentrySdkCaptureClient.class);
    }

    @Test
    void sentryEnabledWithBlankDsnUsesNoOpCaptureClient() {
        SentryCaptureClient client = new SentryMonitoringConfiguration().sentryCaptureClient(
                new SentryMonitoringProperties(true, "", "prod", "release", 0.1)
        );

        assertThat(client).isNotInstanceOf(SentrySdkCaptureClient.class);
    }

    private SamplingContext samplingContext(String transactionName) {
        return new SamplingContext(new TransactionContext(transactionName, "http.server"), null);
    }
}
