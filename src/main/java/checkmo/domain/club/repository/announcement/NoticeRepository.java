package checkmo.domain.club.repository.announcement;

import checkmo.domain.club.entity.announcement.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
