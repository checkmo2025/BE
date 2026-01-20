package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.NoticeComment;
import java.util.List;

public interface NoticeCommentRepositoryCustom {
    List<NoticeComment> findAllByNoticeIdAndCursorPaging(Long noticeId, Long cursorId, Integer size);
}
