package checkmo.common.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.book.internal.exception.BookErrorStatus;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

class Yes24MetricsTest {

    @Test
    void monitoringConfigurationExportsHistogramsUsedByDashboard() throws Exception {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-monitoring.yml"));
        Properties properties = yaml.getObject();
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        try {
            registry.config().meterFilter(new MeterFilter() {
                @Override
                public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                    boolean enabled = Boolean.parseBoolean(properties.getProperty(
                            "management.metrics.distribution.percentiles-histogram." + id.getName()));
                    return DistributionStatisticConfig.builder().percentilesHistogram(enabled).build().merge(config);
                }
            });
            CheckmoMetrics metrics = new CheckmoMetrics(registry);
            metrics.recordYes24ClientResult(metrics.startTimer(), "search", "timeout", null);
            metrics.recordYes24RecommendationRefresh(metrics.startTimer(), "success", "yes24");
            metrics.incrementYes24RecommendationRetry("retry");
            String scrape = registry.scrape();
            assertThat(scrape).contains("checkmo_yes24_client_requests_total",
                    "checkmo_yes24_client_duration_seconds_bucket",
                    "checkmo_yes24_recommendation_refresh_duration_seconds_bucket",
                    "checkmo_yes24_recommendation_retry_count_total").doesNotContain("checkmo_aladin");
            String dashboard = Files.readString(Path.of("monitoring/grafana/dashboards/checkmo-backend-overview.json"));
            assertThat(dashboard).contains("checkmo_yes24_client_requests_total",
                    "checkmo_yes24_client_duration_seconds_bucket").doesNotContain("checkmo_aladin");
        } finally {
            registry.close();
        }
    }

    @Test
    void providerRenamePreservesPublicErrorCodeAndStatus() {
        assertThat(BookErrorStatus.YES24_API_ERROR.getCode()).isEqualTo("BOOK_500");
        assertThat(BookErrorStatus.YES24_API_ERROR.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(BookErrorStatus.YES24_API_ERROR.getMessage()).isEqualTo("예스24 API 호출에 실패했습니다.");
    }
}
