package checkmo.clubManagement.repository;

import checkmo.clubManagement.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom {
    boolean existsByName(String clubName);
}

