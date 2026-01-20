package checkmo.clubNotice.internal.service.query;

import checkmo.clubNotice.internal.entity.NoticeComment;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.repository.NoticeCommentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeCommentQueryService {
    private final NoticeCommentRepository noticeCommentRepository;

    public List<NoticeComment> retrieveNoticeComments(Long noticeId, Long cursorId, Integer size) {
        return noticeCommentRepository.findAllByNoticeIdAndCursorPaging(noticeId, cursorId, size);
    }

    public NoticeComment validateNoticeComment(Long noticeId, Long commentId) {
        return noticeCommentRepository.findByIdAndNoticeId(commentId, noticeId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_COMMENT_NOT_FOUND));
    }
}
