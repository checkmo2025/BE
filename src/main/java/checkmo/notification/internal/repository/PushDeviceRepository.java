package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.PushDevice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {

    Optional<PushDevice> findByInstallationId(String installationId);

    Optional<PushDevice> findByInstallationIdAndMemberId(String installationId, Long memberId);

    Optional<PushDevice> findByExpoPushToken(String expoPushToken);

    List<PushDevice> findAllByMemberIdAndActiveTrue(Long memberId);

    List<PushDevice> findAllByActiveFalseAndDeactivatedAtBefore(LocalDateTime threshold, Pageable pageable);
}
