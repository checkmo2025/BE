package checkmo.domain.club.repository.announcement;

import checkmo.domain.club.entity.announcement.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
        SELECT n FROM Notice n 
        WHERE n.club.id = :clubId
          AND (:onlyImportant = false OR n.important = true)
          AND (:cursorId IS NULL OR n.id < :cursorId)
        ORDER BY n.createdAt DESC
        """)
    List<Notice> findByClubIdAndCursorPaging(
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
    List<Notice> findByClubIdsAndCursorPaging(
            @Param("clubIds") List<Long> clubIds,
            @Param("onlyImportant") boolean onlyImportant,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );


}