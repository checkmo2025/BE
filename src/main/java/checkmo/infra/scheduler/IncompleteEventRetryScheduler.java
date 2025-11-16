package checkmo.infra.scheduler;

import checkmo.infra.scheduler.config.properties.EventRetryProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "events.retry.enabled", havingValue = "true")
public class IncompleteEventRetryScheduler {
    private final EventRetryProperties eventRetryProperties;
    private final IncompleteEventPublications incompleteEventPublications;

    @Scheduled(
            initialDelayString = "${events.retry.initial-delay}",
            fixedDelayString = "${events.retry.fixed-delay}"
    )
    public void retryIncompleteEvents() {
        Duration minDuration = Duration.ofMillis(eventRetryProperties.getMinDuration());

        incompleteEventPublications.resubmitIncompletePublicationsOlderThan(minDuration);
    }

}
