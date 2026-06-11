package checkmo.common.monitoring;

import io.sentry.Sentry;

public class SentrySdkCaptureClient implements SentryCaptureClient {

    public SentrySdkCaptureClient() {
    }

    @Override
    public void captureException(Throwable exception) {
        Sentry.captureException(exception);
    }
}
