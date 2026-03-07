package checkmo.realtime.internal.exception;

public record RealtimeErrorMessage(
        String code,
        String message,
        String command,
        String destination
) {
    public static RealtimeErrorMessage of(RealtimeErrorStatus status, String command, String destination) {
        return new RealtimeErrorMessage(status.getCode(), status.getMessage(), command, destination);
    }
}
