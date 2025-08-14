package checkmo.domain.club.repository.announcement;

import checkmo.domain.club.entity.announcement.Vote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("""
            SELECT v FROM Vote v
            WHERE v.club.id = :clubId
              AND (:onlyImportant = false OR v.important = true)
              AND (:cursorId IS NULL OR v.id < :cursorId)
            ORDER BY v.createdAt DESC
            """)
    List<Vote> findByClubIdAndCursorPaging(
            @Param("clubId") Long clubId,
            @Param("onlyImportant") boolean onlyImportant,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT v FROM Vote v 
            WHERE v.club.id IN :clubIds
              AND (:onlyImportant = false OR v.important = true)
              AND (:cursorId IS NULL OR v.id < :cursorId)
            ORDER BY v.createdAt DESC
            """)
    List<Vote> findByClubIdsAndCursorPaging(
            @Param("clubIds") List<Long> clubIds,
            @Param("onlyImportant") boolean onlyImportant,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    Optional<Vote> findByIdAndClubId(Long id, Long clubId);
}