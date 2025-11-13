package checkmo.clubNotice.internal.service.command;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubNotice.internal.converter.ClubNoticeConverter;
import checkmo.clubNotice.internal.entity.MemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.internal.repository.MemberVoteRepository;
import checkmo.clubNotice.internal.repository.NoticeRepository;
import checkmo.clubNotice.internal.repository.VoteRepository;
import checkmo.clubNotice.internal.service.query.ClubNoticeQueryService;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.CreateClubNoticeDTO;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.CreateClubVoteDTO;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO.VoteResultDTO;
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

    // 외부의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 QueryService
    private final ClubNoticeQueryService clubNoticeQueryService;

    // 자신의 Repository
    private final VoteRepository voteRepository;
    private final NoticeRepository noticeRepository;
    private final MemberVoteRepository memberVoteRepository;

    @Override
    public Notice createPureNotice(Long clubId, String memberId, CreateClubNoticeDTO request) {
        // 1. 유효성 검증 (club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진 여부 확인
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 공지사항 생성 및 저장
        Notice notice = ClubNoticeConverter.fromCreateNoticeDTOToNotice(request, club);
        noticeRepository.save(notice);

        // 4. 공지사항 ID 반환
        return notice;
    }

    @Override
    public void deletePureNotice(Long clubId, Long noticeId, String memberId) {
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진 여부 확인
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 공지사항 존재 여부 및 "순수" 공지사항 여부 확인
        Notice notice = clubNoticeQueryService.validateNotice(clubId, noticeId);
        if ("모임".equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_MEETING_DELETE_FORBIDDEN);
        }

        // 4. 공지사항 삭제
        noticeRepository.delete(notice);
    }

    @Override
    public Vote createVote(Long clubId, String memberId, CreateClubVoteDTO request) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진 여부 확인
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 투표 생성 및 저장
        Vote vote = ClubNoticeConverter.fromCreateVoteDTOToVote(request, clubId);
        //TODO: 데드라인이 현재 시간보다 이전인지, 시작시간이 데드라인보다 이전인지, 시작시간이 현재시간보다 이전인지 검증이 필요하지 않나
        voteRepository.save(vote);

        // 4. 투표 ID 반환
        return vote;
    }

    @Override
    public void deleteVote(Long clubId, Long voteId, String memberId) {
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 운영진 여부 확인
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 투표 조회 및 존재 여부 확인
        Vote vote = clubNoticeQueryService.validateVote(clubId, voteId);

        // 4. 삭제
        voteRepository.delete(vote);
    }

    @Override
    public Long haveVote(Long clubId, Long voteId, String memberId, VoteResultDTO request) {
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 투표 참여자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 3. 투표 조회 및 존재 여부 확인
        Vote vote = clubNoticeQueryService.validateVote(clubId, voteId);

        // 4. 투표 가능 시간인지 검증
        validateVotingTime(vote);

        // 5. 투표의 복수 선택이 불가능하다면 여러 항목 선택했는지 검증
        if (!vote.isDuplication()) {
            if (request.countSelectedItems() > 1) {
                throw new GeneralException(ErrorStatus.MULTIPLE_SELECTION_NOT_ALLOWED);
            }
        }

        // 6. 기존 투표 내역 삭제
        memberVoteRepository.deleteByVoteIdAndMemberId(voteId, clubMember.getMemberId());

        // 7. MemberVote 생성 및 저장
        MemberVote memberVote = ClubNoticeConverter.fromVoteRequestToMemberVote(
                vote, clubMember.getMemberId(), request
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
