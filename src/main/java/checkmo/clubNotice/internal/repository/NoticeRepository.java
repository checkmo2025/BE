package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.Notice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    Optional<Notice> findByIdAndClubId(Long id, Long clubId);

    Optional<Notice> findByMeetingId(Long meetingId);
}