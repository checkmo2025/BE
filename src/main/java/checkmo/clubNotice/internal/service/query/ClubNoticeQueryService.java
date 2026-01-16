package checkmo.clubNotice.internal.service.query;

import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubNoticeQueryService {
    private final NoticeRepository noticeRepository;

    public Optional<Notice> retrieveNoticeDetailWithVoteAndClubMemberVotes(Long clubId, Long noticeId) {
        return noticeRepository.findWithVoteAndClubMemberVotesByIdAndClubId(noticeId, clubId);
    }

    public List<Notice> retrieveNotices(Long clubId, boolean onlyImportant, Long cursorId, Integer size) {
        return noticeRepository.findAllByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, size);
    }

    public Notice validateNotice(Long clubId, Long noticeId) throws ClubNoticeException {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));
    }

}
