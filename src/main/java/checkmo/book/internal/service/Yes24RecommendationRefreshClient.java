package checkmo.book.internal.service;

import checkmo.book.internal.config.properties.Yes24Properties;
import checkmo.book.internal.config.properties.Yes24Properties.Retry;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.service.query.Yes24ApiService;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.common.monitoring.CheckmoMetrics;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.Sleeper;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
public class Yes24RecommendationRefreshClient {

    private final Yes24ApiService yes24ApiService;
    private final CheckmoMetrics checkmoMetrics;
    private final RetryTemplate retryTemplate;

    @Autowired
    public Yes24RecommendationRefreshClient(
            Yes24ApiService yes24ApiService,
            Yes24Properties yes24Properties,
            CheckmoMetrics checkmoMetrics
    ) {
        this(yes24ApiService, yes24Properties, checkmoMetrics, Thread::sleep);
    }

    Yes24RecommendationRefreshClient(
            Yes24ApiService yes24ApiService,
            Yes24Properties yes24Properties,
            CheckmoMetrics checkmoMetrics,
            Sleeper sleeper
    ) {
        this.yes24ApiService = yes24ApiService;
        this.checkmoMetrics = checkmoMetrics;
        this.retryTemplate = buildRetryTemplate(yes24Properties.getRecommendation().getRefresh().getRetry(), sleeper);
    }

    public BookResponseDTO.BookList retrieveRecommendedBooks() {
        return retryTemplate.execute(context -> {
            if (context.getRetryCount() > 0) {
                checkmoMetrics.incrementYes24RecommendationRetry("retry");
                log.warn("알라딘 추천 책 갱신 재시도. attempt={}", context.getRetryCount() + 1);
            }
            return yes24ApiService.retrieveRecommendedBooks();
        });
    }

    private RetryTemplate buildRetryTemplate(Retry retry, Sleeper sleeper) {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new TransientYes24RetryPolicy(retry.getAttempts()));

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retry.getInitialBackoff().toMillis());
        backOffPolicy.setMultiplier(retry.getMultiplier());
        backOffPolicy.setMaxInterval(retry.getMaxBackoff().toMillis());
        backOffPolicy.setSleeper(sleeper);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    private static final class TransientYes24RetryPolicy implements RetryPolicy {

        private final int maxAttempts;

        private TransientYes24RetryPolicy(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        @Override
        public boolean canRetry(RetryContext context) {
            Throwable lastThrowable = context.getLastThrowable();
            return context.getRetryCount() < maxAttempts
                    && (lastThrowable == null || isRetryableYes24AccessFailure(lastThrowable));
        }

        @Override
        public RetryContext open(RetryContext parent) {
            return new RetryContextSupport(parent);
        }

        @Override
        public void close(RetryContext context) {
        }

        @Override
        public void registerThrowable(RetryContext context, Throwable throwable) {
            ((RetryContextSupport) context).registerThrowable(throwable);
        }

        private boolean isRetryableYes24AccessFailure(Throwable throwable) {
            Throwable current = throwable;
            boolean wrappedByYes24BookException = false;

            while (current != null) {
                if (current instanceof IllegalArgumentException) {
                    return false;
                }

                if (current instanceof BookException bookException) {
                    if (bookException.getErrorCode() != BookErrorStatus.ALADIN_API_ERROR) {
                        return false;
                    }
                    wrappedByYes24BookException = true;
                    if (current.getCause() == null) {
                        return false;
                    }
                } else if (current instanceof RestClientResponseException responseException) {
                    return responseException.getStatusCode().is5xxServerError();
                } else if (current instanceof RestClientException || current instanceof IOException) {
                    return true;
                } else if (!wrappedByYes24BookException) {
                    return false;
                }

                current = current.getCause();
            }

            return false;
        }
    }
}
