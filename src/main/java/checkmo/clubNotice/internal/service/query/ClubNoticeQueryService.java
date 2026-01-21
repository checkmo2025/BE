package checkmo.clubNotice.internal.service.query;

import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<Notice> retrieveNotices(Long clubId, boolean important, Pageable pageable) {
        return noticeRepository.findAllByClubIdAndImportant(clubId, important, pageable);
    }

    public Notice validateNotice(Long clubId, Long noticeId) throws ClubNoticeException {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));
    }
}
