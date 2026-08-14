package checkmo.authentication.internal.repository;

import checkmo.authentication.internal.entity.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);
    Optional<AuthUser> findByEmailIgnoreCase(String email);
    Optional<AuthUser> findByNickNameKey(String nickNameKey);
    Optional<AuthUser> findByIdAndDeactivatedAtIsNotNull(Long id);
    Optional<AuthUser> findByProviderAndProviderUserId(String provider, String providerUserId);

    boolean existsByEmail(String email);

}
