package checkmo.clubNotice.internal.service.query;

import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.repository.ClubMemberVoteRepository;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import checkmo.clubNotice.internal.repository.VoteRepository;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOTICE_NOT_FOUND));
    }

    @Override
    public Vote getVote(Long clubId, Long voteId) {
        return voteRepository.findByIdAndClubId(voteId, clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.VOTE_NOT_FOUND));
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
    public List<Notice> getNoticeList(Long clubId, boolean onlyImportant, Long cursorId, Pageable pageable) {
        return noticeRepository.findAllByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, pageable);
    }

    @Override
    public List<Vote> getVoteList(Long clubId, boolean onlyImportant, Long cursorId, Pageable pageable) {
        return voteRepository.findByClubIdAndCursorPaging(clubId, onlyImportant, cursorId, pageable);
    }

    @Override
    public Notice validateNotice(Long clubId, Long noticeId) throws GeneralException {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOTICE_NOT_FOUND));
    }

    @Override
    public Vote validateVote(Long clubId, Long voteId) throws GeneralException {
        return voteRepository.findByIdAndClubId(voteId, clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.VOTE_NOT_FOUND));
    }
}