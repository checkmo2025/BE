package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.repository.projection.ClubIdAndNameProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom {
    boolean existsByName(String clubName);

    @Query("select c.id as id, c.name as name from Club c where c.id in :clubIds")
    List<ClubIdAndNameProjection> findIdAndNameByIdIn(@Param("clubIds") List<Long> clubIds);
}

