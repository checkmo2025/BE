package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Terms;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
    List<Terms> findAllByActiveTrue();

    Optional<Terms> findByIdAndActiveTrue(Long id);
}
