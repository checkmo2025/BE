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

    private final VoteRepository voteRepository;
    private final NoticeRepository noticeRepository;
    private final MemberVoteRepository memberVoteRepository;
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final MemberQueryFacade memberQueryFacade;

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

