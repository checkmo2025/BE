package checkmo.common.monitoring;

import checkmo.common.config.SentryMonitoringProperties;
import checkmo.common.config.SentrySanitizingBeforeSendCallback;
import io.sentry.Sentry;

public class SentrySdkCaptureClient implements SentryCaptureClient {

    public SentrySdkCaptureClient(
            SentryMonitoringProperties properties,
            SentrySanitizingBeforeSendCallback beforeSendCallback
    ) {
        Sentry.init(options -> {
            options.setDsn(properties.dsn());
            options.setEnvironment(properties.environment());
            options.setRelease(properties.release());
            options.setSendDefaultPii(false);
            options.setMaxRequestBodySize(io.sentry.SentryOptions.RequestSize.NONE);
            options.setBeforeSend(beforeSendCallback);
        });
    }

    @Override
    public void captureException(Throwable exception) {
        Sentry.captureException(sanitize(exception));
    }

    static Throwable sanitize(Throwable exception) {
        RuntimeException sanitized = new RuntimeException("Unexpected backend exception");
        if (exception != null) {
            sanitized.setStackTrace(exception.getStackTrace());
        }
        return sanitized;
    }
}
