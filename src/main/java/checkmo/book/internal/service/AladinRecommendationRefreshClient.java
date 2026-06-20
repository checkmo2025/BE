package checkmo.book.internal.service;

import checkmo.book.internal.config.properties.AladinProperties;
import checkmo.book.internal.config.properties.AladinProperties.Retry;
import checkmo.book.internal.exception.BookErrorStatus;
import checkmo.book.internal.exception.BookException;
import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.web.dto.BookResponseDTO;
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

@Slf4j
@Service
public class AladinRecommendationRefreshClient {

    private final AladinApiService aladinApiService;
    private final RetryTemplate retryTemplate;

    @Autowired
    public AladinRecommendationRefreshClient(
            AladinApiService aladinApiService,
            AladinProperties aladinProperties
    ) {
        this(aladinApiService, aladinProperties, Thread::sleep);
    }

    AladinRecommendationRefreshClient(
            AladinApiService aladinApiService,
            AladinProperties aladinProperties,
            Sleeper sleeper
    ) {
        this.aladinApiService = aladinApiService;
        this.retryTemplate = buildRetryTemplate(aladinProperties.getRecommendation().getRefresh().getRetry(), sleeper);
    }

    public BookResponseDTO.BookList retrieveRecommendedBooks() {
        return retryTemplate.execute(context -> {
            if (context.getRetryCount() > 0) {
                log.warn("알라딘 추천 책 갱신 재시도. attempt={}", context.getRetryCount() + 1);
            }
            return aladinApiService.retrieveRecommendedBooks();
        });
    }

    private RetryTemplate buildRetryTemplate(Retry retry, Sleeper sleeper) {
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new TransientAladinRetryPolicy(retry.getAttempts()));

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retry.getInitialBackoff().toMillis());
        backOffPolicy.setMultiplier(retry.getMultiplier());
        backOffPolicy.setMaxInterval(retry.getMaxBackoff().toMillis());
        backOffPolicy.setSleeper(sleeper);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }

    private static final class TransientAladinRetryPolicy implements RetryPolicy {

        private final int maxAttempts;

        private TransientAladinRetryPolicy(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        @Override
        public boolean canRetry(RetryContext context) {
            Throwable lastThrowable = context.getLastThrowable();
            return context.getRetryCount() < maxAttempts
                    && (lastThrowable == null || isRetryableAladinAccessFailure(lastThrowable));
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

        private boolean isRetryableAladinAccessFailure(Throwable throwable) {
            Throwable current = throwable;
            boolean wrappedByAladinBookException = false;

            while (current != null) {
                if (current instanceof IllegalArgumentException) {
                    return false;
                }

                if (current instanceof BookException bookException) {
                    if (bookException.getErrorCode() != BookErrorStatus.ALADIN_API_ERROR) {
                        return false;
                    }
                    wrappedByAladinBookException = true;
                    if (current.getCause() == null) {
                        return false;
                    }
                } else if (current instanceof RestClientException || current instanceof IOException) {
                    return true;
                } else if (!wrappedByAladinBookException) {
                    return false;
                }

                current = current.getCause();
            }

            return false;
        }
    }
}
