package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.NoticeComment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long>, NoticeCommentRepositoryCustom {
    Optional<NoticeComment> findByIdAndNoticeId(Long commentId, Long noticeId);
}
