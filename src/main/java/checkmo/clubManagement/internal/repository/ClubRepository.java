package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom {
    boolean existsByName(String clubName);
}

