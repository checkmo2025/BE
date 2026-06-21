package checkmo.common.monitoring;

public interface SentryCaptureClient {

    void captureException(Throwable exception);
}
