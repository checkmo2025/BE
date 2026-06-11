package checkmo.common.apiPayload.exception;

import checkmo.common.monitoring.SentryCaptureClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("sentry-qa")
public class SentryQaCaptureConfiguration {

    @Bean
    public SentryQaCaptureState sentryQaCaptureState() {
        return new SentryQaCaptureState();
    }

    @Bean
    @Primary
    public SentryCaptureClient sentryQaCaptureClient(SentryQaCaptureState state) {
        return exception -> state.increment();
    }
}
