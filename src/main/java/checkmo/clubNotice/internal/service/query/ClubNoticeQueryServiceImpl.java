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

@Service
@RequiredArgsConstructor
public class ClubNoticeQueryServiceImpl implements ClubNoticeQueryService {

    private final NoticeRepository noticeRepository;
    private final VoteRepository voteRepository;
    private final ClubMemberVoteRepository clubMemberVoteRepository;

    @Override
    public Notice getNotice(Long clubId, Long noticeId) {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));
    }

    @Override
    public Vote getVote(Long clubId, Long voteId) {
        return voteRepository.findByIdAndClubId(voteId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.VOTE_NOT_FOUND));
    }

    @Override
    public List<ClubMemberVote> getMemberVotesByVoteId(Long voteId) {
        return clubMemberVoteRepository.findAllByVoteId(voteId);
    }

    @Override
    public ClubMemberVote getMyVote(Long voteId, Long clubMemberId) {
        return clubMemberVoteRepository.findByVoteIdAndClubMemberId(voteId, clubMemberId).orElse(null);
    }

    @Override
    public List<Notice> getNoticeList(Long clubId, boolean onlyImportant, Long cursorId, Integer size) {
        return noticeRepository.findAllByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, size);
    }

    @Override
    public List<Vote> getVoteList(Long clubId, boolean onlyImportant, Long cursorId, Integer size) {
        return voteRepository.findByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, size);
    }

    @Override
    public Notice validateNotice(Long clubId, Long noticeId) throws ClubNoticeException {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.NOTICE_NOT_FOUND));
    }

    @Override
    public Vote validateVote(Long clubId, Long voteId) throws ClubNoticeException {
        return voteRepository.findByIdAndClubId(voteId, clubId)
                .orElseThrow(() -> new ClubNoticeException(ClubNoticeErrorStatus.VOTE_NOT_FOUND));
    }
}