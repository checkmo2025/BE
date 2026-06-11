package checkmo.common.apiPayload.exception;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.common.monitoring.RecordingSentryCaptureClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class ExceptionAdvicePrivacyTest {

    @Test
    void unexpectedExceptionResponseDoesNotExposeRawMessage() {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);
        String secretMessage = "secret-refresh-token";

        ResponseEntity<Object> response = advice.exception(
                new RuntimeException(secretMessage),
                new ServletWebRequest(new MockHttpServletRequest())
        );

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isInstanceOf(ApiResponse.class);

        ApiResponse<?> body = (ApiResponse<?>) response.getBody();
        assertThat(body.getCode()).isEqualTo("COMMON_500");
        assertThat(body.getMessage()).isEqualTo("서버 에러, 관리자에게 문의 바랍니다.");
        assertThat(body.getResult()).isNull();
        assertThat(response.toString()).doesNotContain(secretMessage);
    }
}
