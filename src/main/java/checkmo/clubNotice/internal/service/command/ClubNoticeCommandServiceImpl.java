package checkmo.clubNotice.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreatedEvent;
import checkmo.clubNotice.internal.converter.ClubNoticeConverter;
import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.repository.ClubMemberVoteRepository;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import checkmo.clubNotice.internal.repository.VoteRepository;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.CreateClubNotice;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.CreateClubVote;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.VoteResult;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ClubNoticeCommandServiceImpl implements ClubNoticeCommandService {

    private final ClubManagementAPI clubManagementAPI;

    private final ClubNoticeQueryService clubNoticeQueryService;

    private final VoteRepository voteRepository;
    private final NoticeRepository noticeRepository;
    private final ClubMemberVoteRepository clubMemberVoteRepository;

    @Override
    public Notice createPureNotice(Long clubId, String memberId, CreateClubNotice request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        Notice notice = ClubNoticeConverter.toNotice(request, clubId);
        noticeRepository.save(notice);

        return notice;
    }

    @Override
    public void deletePureNotice(Long clubId, Long noticeId, String memberId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        if ("모임".equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_MEETING_DELETE_FORBIDDEN);
        }

        noticeRepository.delete(notice);
    }

    @Override
    public void createMeetingNotice(ClubMeetingCreatedEvent event) {
        clubManagementAPI.validateClub(event.clubId());

        Notice existingNotice = noticeRepository.findByMeetingId(event.meetingId()).orElse(null);

        // 기존 공지가 있고, 그 공지가  같은 버전이거나 최신 버전이라면 이벤트 무시
        if (existingNotice != null && existingNotice.isNotOlderThan(event.version())) {
            return;
        }

        // 기존 공지사항이 존재하면 삭제하고 새로 생성 (비즈니스 요구사항)
        if (existingNotice != null) {
            noticeRepository.delete(existingNotice);
        }

        Notice notice = ClubNoticeConverter.toNotice(event);
        noticeRepository.save(notice);
    }

    @Override
    public Vote createVote(Long clubId, String memberId, CreateClubVote request) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        Vote vote = ClubNoticeConverter.toVote(request, clubId);
        //TODO: 데드라인이 현재 시간보다 이전인지, 시작시간이 데드라인보다 이전인지, 시작시간이 현재시간보다 이전인지 검증이 필요하지 않나
        voteRepository.save(vote);

        return vote;
    }

    @Override
    public void deleteVote(Long clubId, Long voteId, String memberId) {
        clubManagementAPI.validateClub(clubId);
        clubManagementAPI.validateStaffClubMember(clubId, memberId);

        Vote vote = clubNoticeQueryService.validateVote(clubId, voteId);

        voteRepository.delete(vote);
    }

    @Override
    public Long haveVote(Long clubId, Long voteId, String memberId, VoteResult request) {
        clubManagementAPI.validateClub(clubId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(clubId, memberId);

        Vote vote = clubNoticeQueryService.validateVote(clubId, voteId);
        validateVotingTime(vote);

        // 투표의 복수 선택이 불가능하다면 여러 항목 선택했는지 검증
        if (!vote.isDuplication()) {
            if (request.countSelectedItems() > 1) {
                throw new GeneralException(ErrorStatus.MULTIPLE_SELECTION_NOT_ALLOWED);
            }
        }

        // 기존 투표 내역 삭제
        clubMemberVoteRepository.deleteByVoteIdAndClubMemberId(voteId, clubMemberId);

        // ClubMemberVote 생성 및 저장
        ClubMemberVote clubMemberVote
                = ClubNoticeConverter.toClubMemberVote(vote, clubMemberId, request);
        clubMemberVoteRepository.save(clubMemberVote);

        return vote.getId();
    }

    private void validateVotingTime(Vote vote) {
        LocalDateTime now = LocalDateTime.now();
        if ((vote.getStartTime() != null && now.isBefore(vote.getStartTime())) ||
                (vote.getDeadline() != null && now.isAfter(vote.getDeadline()))) {
            throw new GeneralException(ErrorStatus.VOTE_TIME_EXPIRED);
        }
    }
}
