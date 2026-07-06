package checkmo.notification.internal.service.command;

import checkmo.notification.internal.entity.PushDevice;
import checkmo.notification.internal.repository.PushDeviceRepository;
import checkmo.notification.web.dto.PushDeviceRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PushDeviceCommandService {

    private final PushDeviceRepository pushDeviceRepository;

    public PushDevice upsert(Long memberId, PushDeviceRequestDTO request) {
        String installationId = (request.getInstallationId() != null)
                ? request.getInstallationId()
                : UUID.randomUUID().toString();
        String token = request.getExpoPushToken();

        PushDevice device;
        var existing = pushDeviceRepository.findByInstallationId(installationId);

        if (existing.isPresent()) {
            device = existing.get();
            // token이 바뀐 경우에만 다른 installation에 같은 token이 있는지 확인한다.
            // token이 동일하면 자기 자신만 존재할 수 있으므로 DB 조회를 생략한다.
            if (!device.getExpoPushToken().equals(token)) {
                deactivateTokenConflict(token, installationId);
            }
            device.update(token, memberId, request.getPlatform(), request.getAppVersion(), request.getBuildNumber());
        } else {
            // 신규 installation이므로 excludeInstallationId를 null로 넘겨 같은 token을 가진 모든 installation을 비활성화한다.
            deactivateTokenConflict(token, null);
            device = PushDevice.builder()
                    .installationId(installationId)
                    .expoPushToken(token)
                    .platform(request.getPlatform())
                    .appVersion(request.getAppVersion())
                    .buildNumber(request.getBuildNumber())
                    .memberId(memberId)
                    .lastRegisteredAt(LocalDateTime.now())
                    .build();
            try {
                pushDeviceRepository.save(device);
            } catch (DataIntegrityViolationException e) {
                // 동시 요청으로 같은 installationId가 이미 생성된 경우 재조회 후 갱신
                device = pushDeviceRepository.findByInstallationId(installationId).orElseThrow();
                device.update(token, memberId, request.getPlatform(), request.getAppVersion(), request.getBuildNumber());
            }
        }

        return device;
    }

    public void deactivate(Long memberId, String installationId) {
        // 미존재·이미 비활성·타인 소유 모두 성공으로 처리한다
        pushDeviceRepository.findByInstallationId(installationId)
                .filter(d -> memberId.equals(d.getMemberId()) && d.isActive())
                .ifPresent(PushDevice::deactivate);
    }

    private void deactivateTokenConflict(String token, String excludeInstallationId) {
        // saveAndFlush로 즉시 UPDATE를 실행해 token=null을 DB에 반영한 뒤 새 device를 INSERT한다.
        // 순서를 보장하지 않으면 Hibernate가 INSERT를 먼저 실행하여 UNIQUE 위반이 발생한다.
        pushDeviceRepository.findByExpoPushToken(token)
                .filter(d -> !d.getInstallationId().equals(excludeInstallationId))
                .ifPresent(d -> {
                    d.deactivate();
                    pushDeviceRepository.saveAndFlush(d);
                });
    }
}
