package checkmo.realtime.web.websocket.error;

import checkmo.realtime.internal.exception.RealtimeErrorStatus;

import java.util.List;

public record RealtimeErrorMessage(
        String code,
        String message,
        List<FieldError> fieldErrors
) {
    public static RealtimeErrorMessage of(RealtimeErrorStatus status) {
        return new RealtimeErrorMessage(status.getCode(), status.getMessage(), List.of());
    }

    public static RealtimeErrorMessage of(RealtimeErrorStatus status, List<FieldError> fieldErrors) {
        List<FieldError> safeFieldErrors = (fieldErrors == null) ? List.of() : fieldErrors;
        return new RealtimeErrorMessage(status.getCode(), status.getMessage(), safeFieldErrors);
    }

    public record FieldError(
            String field,
            String reason
    ) {
    }
}
