package checkmo.authentication.internal.repository;

import checkmo.authentication.internal.entity.AuthUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);
    Optional<AuthUser> findByIdAndDeactivatedAtIsNotNull(Long id);
    Optional<AuthUser> findByProviderAndProviderUserId(String provider, String providerUserId);

    boolean existsByEmail(String email);

    @Query("SELECT a FROM AuthUser a WHERE a.email = :identifier OR a.nickName = :identifier")
    Optional<AuthUser> findByIdentifier(@Param("identifier") String identifier);
}
