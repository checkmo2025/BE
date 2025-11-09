package checkmo.clubNotice.internal.service.query;

import checkmo.clubNotice.internal.entity.MemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.repository.MemberVoteRepository;
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
    private final MemberVoteRepository memberVoteRepository;

    @Override
    public Notice getNotice(Long clubId, Long noticeId) {
        return noticeRepository.findByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOTICE_NOT_FOUND));
    }

    @Override
    public Notice getNoticeWithMeeting(Long clubId, Long noticeId) {
        return noticeRepository.findWithMeetingByIdAndClubId(noticeId, clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOTICE_NOT_FOUND));
    }

    @Override
    public Vote getVote(Long clubId, Long voteId) {
        return voteRepository.findByIdAndClubId(voteId, clubId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.VOTE_NOT_FOUND));
    }

    @Override
    public List<MemberVote> getMemberVotesByVoteId(Long voteId) {
        return memberVoteRepository.findAllByVoteId(voteId);
    }

    @Override
    public MemberVote getMyVote(Long voteId, String memberId) {
        return memberVoteRepository.findByVoteIdAndMemberId(voteId, memberId).orElse(null);
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
    public List<Notice> getNoticeListByClubIds(List<Long> clubIds, boolean onlyImportant, Long cursorId,
                                               Pageable pageable) {
        return noticeRepository.findAllByClubIdsAndCursorPaging(clubIds, onlyImportant, cursorId, pageable);
    }

    @Override
    public List<Vote> getVoteListByClubIds(List<Long> clubIds, boolean onlyImportant, Long cursorId,
                                           Pageable pageable) {
        return voteRepository.findByClubIdsAndCursorPaging(clubIds, onlyImportant, cursorId, pageable);
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