package checkmo.appVersion.internal.repository;

import checkmo.appVersion.internal.entity.AppPlatform;
import checkmo.appVersion.internal.entity.AppVersionPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppVersionPolicyRepository extends JpaRepository<AppVersionPolicy, Long> {
    Optional<AppVersionPolicy> findByPlatformAndActiveTrue(AppPlatform platform);
}
