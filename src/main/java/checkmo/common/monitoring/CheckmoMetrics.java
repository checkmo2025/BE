package checkmo.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CheckmoMetrics {

    private final MeterRegistry meterRegistry;

    public CheckmoMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordAladinClientResult(Timer.Sample sample, String operation, String result, Throwable throwable) {
        Counter.builder("checkmo.aladin.client.requests")
                .tag("operation", operation)
                .tag("result", result)
                .tag("exception", exceptionName(throwable))
                .register(meterRegistry)
                .increment();

        sample.stop(Timer.builder("checkmo.aladin.client.duration")
                .tag("operation", operation)
                .tag("result", result)
                .register(meterRegistry));
    }

    public void recordAladinRecommendationRefresh(Timer.Sample sample, String result, String source) {
        sample.stop(Timer.builder("checkmo.aladin.recommendation.refresh.duration")
                .tag("result", result)
                .tag("source", source)
                .register(meterRegistry));
    }

    public void incrementAladinRecommendationRetry(String result) {
        Counter.builder("checkmo.aladin.recommendation.retry.count")
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void incrementRecommendationCacheRequest(String result) {
        Counter.builder("checkmo.book.recommendation.cache.requests")
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void recordRecommendationBooksCount(String source, int count) {
        DistributionSummary.builder("checkmo.book.recommendation.books.count")
                .tag("source", source)
                .register(meterRegistry)
                .record(count);
    }

    public void recordBannerApiDuration(Timer.Sample sample, String result) {
        sample.stop(Timer.builder("checkmo.banner.api.duration")
                .tag("result", result)
                .register(meterRegistry));
    }

    public void incrementBannerCacheRequest(String result) {
        Counter.builder("checkmo.banner.cache.requests")
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void incrementChatbotModelCall(String modelName, boolean escalated) {
        Counter.builder("checkmo.chatbot.model.calls")
                .tag("model", modelName)
                .tag("escalated", String.valueOf(escalated))
                .register(meterRegistry)
                .increment();
    }

    public void incrementChatbotUnresolvedSession() {
        Counter.builder("checkmo.chatbot.session.unresolved")
                .register(meterRegistry)
                .increment();
    }

    public void incrementChatbotPromptLeakDetected() {
        Counter.builder("checkmo.chatbot.prompt_leak.detected")
                .register(meterRegistry)
                .increment();
    }

    public String classifyAladinResult(Throwable throwable) {
        if (throwable == null) {
            return "success";
        }
        if (containsType(throwable, SocketTimeoutException.class)
                || containsType(throwable, TimeoutException.class)
                || containsType(throwable, ResourceAccessException.class)) {
            return "timeout";
        }
        if (containsType(throwable, RestClientResponseException.class)) {
            return "http_error";
        }
        if (throwable instanceof IllegalArgumentException) {
            return "invalid_response";
        }
        return "unknown_error";
    }

    private boolean containsType(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String exceptionName(Throwable throwable) {
        if (throwable == null) {
            return "none";
        }
        return throwable.getClass().getSimpleName();
    }
}
