package checkmo.infra;

public record PushReceiptResult(
        String status,
        String errorCode,
        String errorMessage
) {
    public boolean isOk() {
        return "ok".equals(status);
    }

    public boolean isPending() {
        return status == null || "pending".equals(status);
    }

    public boolean isDeviceNotRegistered() {
        return "error".equals(status) && "DeviceNotRegistered".equals(errorCode);
    }

    public boolean isMessageRateExceeded() {
        return "error".equals(status) && "MessageRateExceeded".equals(errorCode);
    }

    public boolean isPermanentError() {
        return "error".equals(status) && (
                "MessageTooBig".equals(errorCode)
                        || "InvalidCredentials".equals(errorCode)
                        || "MismatchSenderId".equals(errorCode)
        );
    }
}
