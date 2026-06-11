package checkmo.common.apiPayload.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.monitoring.RecordingSentryCaptureClient;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

class SentryExpectedErrorExclusionTest {

    @Test
    void doesNotCaptureGeneralException() {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);

        advice.onThrowException(new GeneralException(ErrorStatus._BAD_REQUEST), new MockHttpServletRequest());

        assertThat(captureClient.count()).isZero();
    }

    @Test
    void doesNotCaptureValidationException() throws NoSuchMethodException {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);
        Method method = SampleController.class.getDeclaredMethod("create", SampleRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new SampleRequest(null), "request");
        bindingResult.addError(new FieldError("request", "name", "required"));

        advice.handleMethodArgumentNotValid(
                new MethodArgumentNotValidException(parameter, bindingResult),
                org.springframework.http.HttpHeaders.EMPTY,
                org.springframework.http.HttpStatus.BAD_REQUEST,
                new ServletWebRequest(new MockHttpServletRequest())
        );

        assertThat(captureClient.count()).isZero();
    }

    @Test
    void doesNotCaptureConstraintViolationException() {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("constraint");
        ConstraintViolationException exception = new ConstraintViolationException(
                "constraint",
                Set.of(violation)
        );

        advice.validation(exception, new ServletWebRequest(new MockHttpServletRequest()));

        assertThat(captureClient.count()).isZero();
    }

    @Test
    void doesNotCaptureAccessDeniedException() {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);

        advice.accessDenied(
                new AccessDeniedException("denied"),
                new ServletWebRequest(new MockHttpServletRequest())
        );

        assertThat(captureClient.count()).isZero();
    }

    @Test
    void doesNotCaptureAuthenticationException() {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);

        advice.authentication(
                new AuthenticationCredentialsNotFoundException("unauthenticated"),
                new ServletWebRequest(new MockHttpServletRequest())
        );

        assertThat(captureClient.count()).isZero();
    }

    @Test
    void doesNotCaptureIllegalArgumentException() {
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        ExceptionAdvice advice = new ExceptionAdvice(captureClient);

        advice.handleIllegalArgumentException(
                new IllegalArgumentException("bad request"),
                new ServletWebRequest(new MockHttpServletRequest())
        );

        assertThat(captureClient.count()).isZero();
    }

    private record SampleRequest(String name) {
    }

    private static final class SampleController {

        @SuppressWarnings("unused")
        void create(SampleRequest request) {
        }
    }
}
