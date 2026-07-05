package checkmo.infra.push.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpoTicket(
        String status,
        String id,
        String message,
        ExpoDetails details
) {
    public boolean isDeviceNotRegistered() {
        return "error".equals(status)
                && details != null
                && "DeviceNotRegistered".equals(details.error());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpoDetails(String error) {
    }
}
