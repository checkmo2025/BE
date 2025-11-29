package checkmo.clubNotice.internal.service.query;

import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.exception.ClubNoticeErrorStatus;
import checkmo.clubNotice.internal.exception.ClubNoticeException;
import checkmo.clubNotice.internal.repository.ClubMemberVoteRepository;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import checkmo.clubNotice.internal.repository.VoteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubNoticeQueryService {
    private final NoticeRepository noticeRepository;
    private final VoteRepository voteRepository;
    private final ClubMemberVoteRepository clubMemberVoteRepository;

    public Notice retrieveNotice(Long clubId, Long noticeId) {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));
    }

    public List<Notice> retrieveNotices(Long clubId, boolean onlyImportant, Long cursorId, Integer size) {
        return noticeRepository.findAllByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, size);
    }

    public Vote retrieveVote(Long clubId, Long voteId) {
        return voteRepository.findByIdAndClubId(voteId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.VOTE_NOT_FOUND));
    }

    public List<Vote> retrieveVotes(Long clubId, boolean onlyImportant, Long cursorId, Integer size) {
        return voteRepository.findByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, size);
    }

    public ClubMemberVote retrieveClubMemberVote(Long voteId, Long clubMemberId) {
        return clubMemberVoteRepository.findByVoteIdAndClubMemberId(voteId, clubMemberId).orElse(null);
    }

    public List<ClubMemberVote> retrieveClubMemberVotes(Long voteId) {
        return clubMemberVoteRepository.findAllByVoteId(voteId);
    }

    public Notice validateNotice(Long clubId, Long noticeId) throws ClubNoticeException {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));
    }

    public Vote validateVote(Long clubId, Long voteId) throws ClubNoticeException {
        return voteRepository.findByIdAndClubId(voteId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.VOTE_NOT_FOUND));
    }
}
