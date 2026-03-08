package checkmo.realtime.internal.config;

import checkmo.realtime.internal.exception.RealtimeErrorMessage;
import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomStompErrorHandler extends StompSubProtocolErrorHandler {
    private final ObjectMapper objectMapper;

    @Override
    public @Nullable Message<byte[]> handleClientMessageProcessingError(
            @Nullable Message<byte[]> clientMessage,
            Throwable e
    ) {
        Throwable mostSpecificCause = NestedExceptionUtils.getMostSpecificCause(e);
        if (mostSpecificCause instanceof RealtimeException exception) {
            return buildErrorFrame(clientMessage, exception.getErrorStatus());
        }

        RealtimeException nestedCause = findCause(e, RealtimeException.class);
        if (nestedCause != null) {
            RealtimeErrorStatus errorStatus = nestedCause.getErrorStatus();
        }
        return super.handleClientMessageProcessingError(clientMessage, e);
    }

    private Message<byte[]> buildErrorFrame(
            @Nullable Message<byte[]> clientMessage,
            RealtimeErrorStatus errorStatus
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(errorStatus.getCode());
        accessor.setLeaveMutable(true);

        byte[] payload;
        try {
            RealtimeErrorMessage body = RealtimeErrorMessage.of(errorStatus);
            payload = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            String fallbackMessage = String.format(
                    "code: %s, message: %s",
                    errorStatus.getCode(), errorStatus.getMessage()
            );
            payload = fallbackMessage.getBytes(StandardCharsets.UTF_8);
        }
        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }

    private <T extends Throwable> T findCause(Throwable ex, Class<T> type) {
        Throwable currentException = ex;
        while (currentException != null) {
            if (type.isInstance(currentException)) {
                return type.cast(currentException);
            }
            currentException = currentException.getCause();
        }
        return null;
    }
}
