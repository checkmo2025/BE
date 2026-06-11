package checkmo.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "checkmo.sentry")
public record SentryMonitoringProperties(
        boolean enabled,
        String dsn,
        String environment,
        String release
) {

    public SentryMonitoringProperties {
        dsn = dsn == null ? "" : dsn;
        environment = environment == null || environment.isBlank() ? "local" : environment;
        release = release == null || release.isBlank() ? "local" : release;
    }
}
