package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.Notice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {

    Optional<Notice> findByIdAndClubId(Long id, Long clubId);

    Optional<Notice> findByMeetingId(Long meetingId);
}