package checkmo.notification.internal.repository;

import checkmo.notification.internal.entity.NotificationSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByMemberId(String memberId);

    boolean existsByMemberId(String memberId);
}