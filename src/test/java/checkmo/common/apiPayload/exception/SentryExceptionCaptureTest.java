package checkmo.common.apiPayload.exception;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.common.monitoring.RecordingSentryCaptureClient;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class SentryExceptionCaptureTest {

    @Test
    void capturesUnhandledExceptionOnce() {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);
        RuntimeException exception = new RuntimeException("unexpected");

        advice.exception(exception, new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(captureClient.count()).isEqualTo(1);
        assertThat(captureClient.captured()).containsExactly(exception);
    }

    @Test
    void capturesUnhandledExceptionThroughCaptureClient() {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);

        advice.exception(new RuntimeException("unexpected"), new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(captureClient.count()).isEqualTo(1);
    }
}
