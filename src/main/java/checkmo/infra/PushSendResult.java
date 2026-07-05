package checkmo.infra;

public record PushSendResult(
        boolean ok,
        String ticketId,
        String errorCode,
        String errorMessage
) {
    public boolean isDeviceNotRegistered() {
        return !ok && "DeviceNotRegistered".equals(errorCode);
    }

    public static PushSendResult ok(String ticketId) {
        return new PushSendResult(true, ticketId, null, null);
    }

    public static PushSendResult failed(String errorCode, String errorMessage) {
        return new PushSendResult(false, null, errorCode, errorMessage);
    }
}
