package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.PushDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {

    Optional<PushDevice> findByInstallationId(String installationId);

    Optional<PushDevice> findByInstallationIdAndMemberId(String installationId, String memberId);

    Optional<PushDevice> findByExpoPushToken(String expoPushToken);

    List<PushDevice> findAllByMemberIdAndActiveTrue(String memberId);
}
