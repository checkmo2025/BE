package checkmo.domain.club.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.MemberVote;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.announcement.Vote;
import checkmo.domain.club.repository.announcement.MemberVoteRepository;
import checkmo.domain.club.repository.announcement.NoticeRepository;
import checkmo.domain.club.repository.announcement.VoteRepository;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ClubCommunicationCommandServiceImpl implements ClubCommunicationCommandService {

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;

    // 자신의 QueryService
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubQueryService clubQueryService;

    // 자신의 Repository
    private final VoteRepository voteRepository;
    private final NoticeRepository noticeRepository;
    private final MemberVoteRepository memberVoteRepository;

    /**
     * 독서 모임에 공지사항을 작성합니다. (모임이랑 연결되지 않은 순수 공지사항)
     *
     * @param clubId 독서 모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param request 공지사항 작성 요청 DTO
     * @return 작성된 공지사항 ID
     */
    @Override
    public Long createNotice(Long clubId, String memberId, ClubRequestDTO.CreateClubNoticeDTO request) {

        // 1. 검증
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. Notice 엔티티 생성 & 저장
        Notice notice = ClubConverter.fromCreateNoticeDTOToNotice(request, club);
        noticeRepository.save(notice);

        return notice.getId();
    }

    /**
     * 독서 모임의 공지사항을 삭제합니다.
     *
     * @param clubId 독서 모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param noticeId 삭제할 공지사항 ID
     */
    @Override
    public void deleteNotice(Long clubId, String memberId, Long noticeId) {

        // 1. 검증
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 공지사항 존재 여부 확인
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOTICE_NOT_FOUND));

        // 3. 공지사항의 태그가 "모임"인 경우 예외 처리(모임 공지사항은 삭제할 수 없음)
        if ("모임".equals(notice.getTag())) {
            throw new GeneralException(ErrorStatus.NOTICE_MEETING_DELETE_FORBIDDEN);
        }

        // 4. 삭제
        noticeRepository.delete(notice);
    }

    /**
     * 독서 모임에 투표를 생성합니다.
     *
     * @param clubId 독서 모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param request 투표 생성 요청 DTO
     * @return 생성된 투표 ID
     */
    @Override
    public Long createVote(Long clubId, String memberId, ClubRequestDTO.CreateClubVoteDTO request) {

        // 1. 검증
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. Vote 엔티티 생성 & 저장
        Vote vote = ClubConverter.fromCreateVoteDTOToVote(request, club);
        voteRepository.save(vote);

        return vote.getId();
    }

    /**
     * 독서 모임에 투표를 삭제합니다.
     *
     * @param clubId 독서 모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param voteId 삭제할 투표 ID
     */
    @Override
    public void deleteVote(Long clubId, String memberId, Long voteId) {

        // 1. 검증
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 투표 존재 여부 확인
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.VOTE_NOT_FOUND));

        // 3. 삭제
        voteRepository.delete(vote);
    }

    /**
     * 독서 모임의 투표에 참여합니다.
     *
     * @param clubId 독서 모임 ID
     * @param memberId 참여자 회원 ID -> 독서 클럽의 회원인지만 확인
     * @param voteId 투표 ID
     * @param request 투표 내역 DTO
     * @return 참여한 투표 ID
     */
    @Override
    public Long participateInPoll(Long clubId, String memberId, Long voteId, ClubRequestDTO.VoteResultDTO request) {

        // 1. 클럽 및 회원 검증
        clubQueryService.validateClub(clubId);
        clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 투표 조회
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.VOTE_NOT_FOUND));

        // 3. 투표 시간 내인지 검증
        LocalDateTime now = LocalDateTime.now();
        if ((vote.getStartTime() != null && now.isBefore(vote.getStartTime())) ||
                (vote.getDeadline() != null && now.isAfter(vote.getDeadline()))) {
            throw new GeneralException(ErrorStatus.VOTE_TIME_EXPIRED);
        }

        // 4. 복수 선택 금지 처리
        if (!vote.isDuplication()) {
            int count = 0;
            if (request.isItem1()) count++;
            if (request.isItem2()) count++;
            if (request.isItem3()) count++;
            if (request.isItem4()) count++;
            if (request.isItem5()) count++;

            if (count > 1) {
                throw new GeneralException(ErrorStatus.MULTIPLE_SELECTION_NOT_ALLOWED);
            }
        }

        // 5. 기존 투표 내역 삭제
        memberVoteRepository.deleteByVoteIdAndMemberId(voteId, memberId);

        // 6. MemberVote 생성 및 저장
        Member memberProxy = memberQueryFacade.findMemberReferenceById(memberId);
        MemberVote memberVote = ClubConverter.fromVoteRequestToMemberVote(
                vote, memberId, memberProxy, request
        );

        memberVoteRepository.save(memberVote);

        return vote.getId();
    }

}
