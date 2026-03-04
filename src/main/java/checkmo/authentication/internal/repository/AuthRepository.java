package checkmo.authentication.internal.repository;

import checkmo.authentication.internal.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthRepository extends JpaRepository<AuthUser, String> {

    Optional<AuthUser> findByEmail(String email);
    Optional<AuthUser> findByIdAndDeactivatedAtIsNotNull(String id);

    boolean existsByEmail(String email);

    @Query("SELECT a FROM AuthUser a WHERE a.email = :identifier OR a.nickName = :identifier")
    Optional<AuthUser> findByIdentifier(@Param("identifier") String identifier);
}
