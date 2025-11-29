package checkmo.authentication.internal.repository;

import checkmo.authentication.internal.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthRepository extends JpaRepository<AuthUser, String> {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
