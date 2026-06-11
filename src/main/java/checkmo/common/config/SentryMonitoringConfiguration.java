package checkmo.common.config;

import checkmo.common.monitoring.SentryCaptureClient;
import checkmo.common.monitoring.SentrySdkCaptureClient;
import io.sentry.SentryOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SentryMonitoringProperties.class)
public class SentryMonitoringConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SentryCaptureClient sentryCaptureClient(SentryMonitoringProperties properties) {
        if (!properties.enabled() || properties.dsn().isBlank()) {
            return exception -> {
            };
        }
        return new SentrySdkCaptureClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public SentryOptions.TracesSamplerCallback sentryTracesSamplerCallback(SentryMonitoringProperties properties) {
        return samplingContext -> {
            if (!properties.enabled() || properties.tracesSampleRate() <= 0.0) {
                return 0.0;
            }
            String transactionName = samplingContext.getTransactionContext().getName();
            if (isNoisyTransaction(transactionName)) {
                return 0.0;
            }
            return properties.tracesSampleRate();
        };
    }

    private boolean isNoisyTransaction(String transactionName) {
        if (transactionName == null) {
            return false;
        }
        return isHealthCheckTransaction(transactionName)
                || transactionName.contains("/swagger-ui")
                || transactionName.contains("/v3/api-docs");
    }

    private boolean isHealthCheckTransaction(String transactionName) {
        return transactionName.equals("/health")
                || transactionName.endsWith(" /health");
    }
}
