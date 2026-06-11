package checkmo.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "checkmo.sentry")
public record SentryMonitoringProperties(
        boolean enabled,
        String dsn,
        String environment,
        String release,
        double tracesSampleRate
) {

    public SentryMonitoringProperties {
        dsn = dsn == null ? "" : dsn;
        environment = environment == null || environment.isBlank() ? "local" : environment;
        release = release == null || release.isBlank() ? "local" : release;
        tracesSampleRate = normalizeSampleRate(tracesSampleRate);
    }

    private static double normalizeSampleRate(double sampleRate) {
        if (Double.isNaN(sampleRate) || sampleRate < 0.0) {
            return 0.0;
        }
        if (sampleRate > 1.0) {
            return 1.0;
        }
        return sampleRate;
    }
}
