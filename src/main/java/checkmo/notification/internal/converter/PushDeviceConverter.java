package checkmo.notification.internal.converter;

import checkmo.notification.internal.entity.PushDevice;
import checkmo.notification.web.dto.PushDeviceResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PushDeviceConverter {

    public static PushDeviceResponseDTO toResponse(PushDevice device) {
        return PushDeviceResponseDTO.builder()
                .deviceId(device.getId())
                .installationId(device.getInstallationId())
                .active(device.isActive())
                .registeredAt(device.getLastRegisteredAt())
                .build();
    }
}
