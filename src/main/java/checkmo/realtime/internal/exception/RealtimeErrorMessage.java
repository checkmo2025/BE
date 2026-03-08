package checkmo.realtime.internal.exception;

public record RealtimeErrorMessage(
        String code,
        String message
) {
    public static RealtimeErrorMessage of(RealtimeErrorStatus status) {
        return new RealtimeErrorMessage(status.getCode(), status.getMessage());
    }
}
