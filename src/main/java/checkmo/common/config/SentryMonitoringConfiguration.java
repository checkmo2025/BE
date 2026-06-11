package checkmo.common.config;

import checkmo.common.monitoring.SentryCaptureClient;
import checkmo.common.monitoring.SentrySdkCaptureClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SentryMonitoringProperties.class)
public class SentryMonitoringConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SentryCaptureClient sentryCaptureClient(
            SentryMonitoringProperties properties,
            SentrySanitizingBeforeSendCallback beforeSendCallback
    ) {
        if (!properties.enabled() || properties.dsn().isBlank()) {
            return exception -> {
            };
        }
        return new SentrySdkCaptureClient(properties, beforeSendCallback);
    }
}
