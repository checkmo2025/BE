package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.Notice;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT DISTINCT n FROM Notice n "
            + "LEFT JOIN FETCH n.vote v "
            + "LEFT JOIN FETCH v.clubMemberVotes cmv "
            + "where n.id = :noticeId AND n.clubId = :clubId")
    Optional<Notice> findWithVoteAndClubMemberVotesByIdAndClubId(Long noticeId, Long clubId);

    Optional<Notice> findByIdAndClubId(Long noticeId, Long clubId);

    Optional<Notice> findByMeetingId(Long meetingId);

    @Query("SELECT n FROM Notice n "
            + "WHERE n.clubId = :clubId "
            + "AND n.important = :onlyImportant "
            + "ORDER BY n.createdAt DESC, n.id DESC ")
    Page<Notice> findAllByClubIdAndCursorPaging(Long clubId, boolean onlyImportant, Pageable pageable);
}