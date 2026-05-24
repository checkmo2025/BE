package checkmo.clubNotice.internal.service.query;

import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubNoticeQueryService {
    private final NoticeRepository noticeRepository;

    public Optional<Notice> retrieveLatestNotice(Long clubId) {
        return noticeRepository.findTop1ByClubIdOrderByCreatedAtDescIdDesc(clubId);
    }

    public Optional<Notice> retrieveNoticeDetailWithVoteAndClubMemberVotes(Long clubId, Long noticeId) {
        return noticeRepository.findWithVoteAndClubMemberVotesByIdAndClubId(noticeId, clubId);
    }

    public List<Notice> retrievePinnedNotices(Long clubId, int limit) {
        return noticeRepository.findTopPinnedByClubId(
                clubId,
                PageRequest.of(0, limit)
        );
    }

    public Page<Notice> retrieveNormalNotices(Long clubId, Pageable pageable) {
        return noticeRepository.findAllByClubIdAndPinned(clubId, false, pageable);
    }

    public Notice validateNotice(Long noticeId) throws ClubNoticeException {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));
    }

    public Notice validateNotice(Long clubId, Long noticeId) throws ClubNoticeException {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));
    }
}
