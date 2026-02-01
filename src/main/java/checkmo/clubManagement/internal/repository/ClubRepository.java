package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.repository.projection.ClubIdAndName;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom {
    boolean existsByName(String clubName);

    @Query("select c.id as id, c.name as name from Club c where c.id in :clubIds")
    List<ClubIdAndName> findIdAndNameByIdIn(@Param("clubIds") List<Long> clubIds);

    @Modifying
    @Query("UPDATE Club c "
            + "SET c.lastActivityAt = case "
            + "WHEN c.lastActivityAt IS NULL OR c.lastActivityAt < :time THEN :time "
            + "ELSE c.lastActivityAt END "
            + "WHERE c.id = :clubId")
    void updateLastActivityTime(Long clubId, LocalDateTime time);
}

