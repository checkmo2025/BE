package checkmo.infra.scheduler.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "events.retry")
public class EventRetryProperties {
    private boolean enabled;
    private Long initialDelay;
    private Long fixedDelay;
    private Long minDuration;
}
