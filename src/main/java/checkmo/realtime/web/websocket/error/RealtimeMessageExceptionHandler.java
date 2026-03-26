package checkmo.realtime.web.websocket.error;

import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;

/**
 * STOMP @MessageMapping 처리 중 발생한 예외를
 * /user/queue/errors 로 표준 에러 메시지(RealtimeErrorMessage)로 내려주는 핸들러입니다.
 */
@Slf4j
@ControllerAdvice
public class RealtimeMessageExceptionHandler {

    // 1) RealtimeException 처리
    @MessageExceptionHandler(RealtimeException.class)
    @SendToUser("/queue/errors")
    public RealtimeErrorMessage handleRealtimeException(RealtimeException e, Message<?> message) {
        RealtimeErrorStatus status = e.getErrorStatus();
        log.warn("RealtimeException 발생: code={}, message={}, headers={}", status.getCode(), status.getMessage(), message.getHeaders());
        return RealtimeErrorMessage.of(status);
    }

    // 2) Validation 처리
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser("/queue/errors")
    public RealtimeErrorMessage handleMethodArgumentNotValidException(MethodArgumentNotValidException e, Message<?> message) {
        List<RealtimeErrorMessage.FieldError> fieldErrors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> new RealtimeErrorMessage.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("MethodArgumentNotValidException 발생: headers={}, e={}", message.getHeaders(), e.getMessage());
        return RealtimeErrorMessage.of(RealtimeErrorStatus.INVALID_PAYLOAD, fieldErrors);
    }

    // 3) 파라미터/헤더 등 제약조건 위반 처리
    @MessageExceptionHandler(ConstraintViolationException.class)
    @SendToUser("/queue/errors")
    public RealtimeErrorMessage handleConstraintViolation(ConstraintViolationException e, Message<?> message) {
        log.warn("ConstraintViolation 발생: headers={}, e={}", message.getHeaders(), e.getMessage());
        return RealtimeErrorMessage.of(RealtimeErrorStatus.INVALID_REQUEST);
    }

    // 4) 메시지 변환 실패 처리
    @MessageExceptionHandler(MessageConversionException.class)
    @SendToUser("/queue/errors")
    public RealtimeErrorMessage handleMessageConversion(MessageConversionException e, Message<?> message) {
        log.warn("MessageConversionException 발생: headers={}, e={}", message.getHeaders(), e.getMessage());
        return RealtimeErrorMessage.of(RealtimeErrorStatus.INVALID_JSON);
    }

    // 5) 그 외 모든 예외는 INTERNAL_SERVER_ERROR로 처리
    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public RealtimeErrorMessage handleUnknown(Exception e, Message<?> message) {
        log.error("Unhandled WS exception 발생: headers={}", message.getHeaders(), e);
        return RealtimeErrorMessage.of(RealtimeErrorStatus.INTERNAL_SERVER_ERROR);
    }
}
