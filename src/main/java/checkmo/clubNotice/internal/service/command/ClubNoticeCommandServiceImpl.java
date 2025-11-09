package checkmo.clubNotice.internal.service.command;

import checkmo.clubManagement.entity.Club;
import checkmo.clubManagement.entity.ClubMember;
import checkmo.clubNotice.converter.ClubNoticeConverter;
import checkmo.clubNotice.entity.MemberVote;
import checkmo.clubNotice.entity.Notice;
import checkmo.clubNotice.entity.Vote;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.repository.MemberVoteRepository;
import checkmo.clubNotice.repository.NoticeRepository;
import checkmo.clubNotice.repository.VoteRepository;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberAPI;
import checkmo.member.entity.Member;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ClubNoticeCommandServiceImpl implements ClubNoticeCommandService {

    // Domain level 2
    private final MemberAPI memberAPI;

    // 자신의 QueryService
    private final ClubNoticeQueryService clubNoticeQueryService;

    // 자신의 Repository
    private final VoteRepository voteRepository;
    private final NoticeRepository noticeRepository;
    private final MemberVoteRepository memberVoteRepository;

    @Override
    public Notice createPureNotice(Club club, ClubMember clubMember, ClubNoticeRequestDTO.CreateClubNoticeDTO request) {
        // 1. 운영진 여부 확인
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 공지사항 생성 및 저장
        Notice notice = ClubNoticeConverter.fromCreateNoticeDTOToNotice(request, club);
        noticeRepository.save(notice);

        // 3. 공지사항 ID 반환
        return notice;
    }

    @Override
    public void deletePureNotice(Long clubId, ClubMember clubMember, Long noticeId) {
        // 1. 운영진 여부 확인
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 공지사항 존재 여부 및 "순수" 공지사항 여부 확인
        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        if ("모임".equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_MEETING_DELETE_FORBIDDEN);
        }

        // 3. 공지사항 삭제
        noticeRepository.delete(notice);
    }

    @Override
    public Vote createVote(Club club, ClubMember clubMember, ClubNoticeRequestDTO.CreateClubVoteDTO request) {
        // 1. 운영진 여부 확인
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 투표 생성 및 저장
        Vote vote = ClubNoticeConverter.fromCreateVoteDTOToVote(request, club);
        //TODO: 데드라인이 현재 시간보다 이전인지, 시작시간이 데드라인보다 이전인지, 시작시간이 현재시간보다 이전인지 검증이 필요하지 않나
        voteRepository.save(vote);

        // 3. 투표 ID 반환
        return vote;
    }

    @Override
    public void deleteVote(Long clubId, ClubMember clubMember, Long voteId) {
        // 1. 운영진 여부 확인
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 투표 조회 및 존재 여부 확인
        Vote vote = clubNoticeQueryService.validateVote(clubId, voteId);

        // 3. 삭제
        voteRepository.delete(vote);
    }

    @Override
    public Long haveVote(Long clubId, ClubMember clubMember, Long voteId, ClubNoticeRequestDTO.VoteResultDTO request) {
        // 1. 투표 참여자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 2. 투표 조회 및 존재 여부 확인
        Vote vote = clubNoticeQueryService.validateVote(clubId, voteId);

        // 3. 투표 가능 시간인지 검증
        validateVotingTime(vote);

        // 4. 투표의 복수 선택이 불가능하다면 여러 항목 선택했는지 검증
        if (!vote.isDuplication()) {
            if (request.countSelectedItems() > 1) {
                throw new GeneralException(ErrorStatus.MULTIPLE_SELECTION_NOT_ALLOWED);
            }
        }

        // 5. 기존 투표 내역 삭제
        memberVoteRepository.deleteByVoteIdAndMemberId(voteId, clubMember.getMemberId());

        // 6. MemberVote 생성 및 저장
        Member memberProxy = memberAPI.findMemberReferenceById(clubMember.getMemberId());
        MemberVote memberVote = ClubNoticeConverter.fromVoteRequestToMemberVote(
                vote, clubMember.getMemberId(), memberProxy, request
        );
        memberVoteRepository.save(memberVote);

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
