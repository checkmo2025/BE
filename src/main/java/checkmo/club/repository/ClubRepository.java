package checkmo.club.repository;

import checkmo.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> , ClubRepositoryCustom {
    boolean existsByName(String clubName);
}

