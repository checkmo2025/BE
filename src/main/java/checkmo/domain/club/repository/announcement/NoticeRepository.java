package checkmo.domain.club.repository.announcement;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.announcement.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            SELECT n FROM Notice n 
            WHERE n.club.id = :clubId
              AND (:onlyImportant = false OR n.important = true)
              AND (:cursorId IS NULL OR n.id < :cursorId)
            ORDER BY n.createdAt DESC
            """)
    List<Notice> findAllByClubIdAndCursorPaging(
            @Param("clubId") Long clubId,
            @Param("onlyImportant") boolean onlyImportant,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            SELECT n FROM Notice n 
            WHERE n.club.id IN :clubIds
              AND (:onlyImportant = false OR n.important = true)
              AND (:cursorId IS NULL OR n.id < :cursorId)
            ORDER BY n.createdAt DESC
            """)
    List<Notice> findAllByClubIdsAndCursorPaging(
            @Param("clubIds") List<Long> clubIds,
            @Param("onlyImportant") boolean onlyImportant,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );


    List<Notice> club(Club club);

    Optional<Notice> findByIdAndClubId(Long id, Long clubId);

    @Query("SELECT n FROM Notice n JOIN FETCH n.meeting m WHERE n.id = :id AND n.club.id = :clubId")
    Optional<Notice> findWithMeetingByIdAndClubId(Long id, Long clubId);
}