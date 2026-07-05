package checkmo.infra.push.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpoReceipt(
        String status,
        String id,
        String message,
        ExpoDetails details
) {
    public boolean isOk() {
        return "ok".equals(status);
    }

    public boolean isDeviceNotRegistered() {
        return "error".equals(status)
                && details != null
                && "DeviceNotRegistered".equals(details.error());
    }

    public boolean isMessageRateExceeded() {
        return "error".equals(status)
                && details != null
                && "MessageRateExceeded".equals(details.error());
    }

    public boolean isPermanentError() {
        if (!"error".equals(status) || details == null) return false;
        String error = details.error();
        return "MessageTooBig".equals(error)
                || "InvalidCredentials".equals(error)
                || "MismatchSenderId".equals(error);
    }

    public String errorCode() {
        return details != null ? details.error() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpoDetails(String error) {
    }
}
