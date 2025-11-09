package checkmo.clubNotice.facade;

import checkmo.clubManagement.entity.Club;
import checkmo.clubManagement.entity.ClubMember;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubNotice.converter.ClubNoticeConverter;
import checkmo.clubNotice.entity.Notice;
import checkmo.clubNotice.entity.Vote;
import checkmo.clubNotice.internal.service.command.ClubNoticeCommandService;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.ClubNoticeDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubNoticeCommandFacadeImpl implements ClubNoticeCommandFacade {

    // 자신의 CommandService
    private final ClubNoticeCommandService clubNoticeCommandService;

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    @Override
    public ClubNoticeDetailDTO createPureNotice(Long clubId, String memberId,
                                                ClubNoticeRequestDTO.CreateClubNoticeDTO request) {
        // 1. 유효성 검증 (club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 공지 생성 및 DTO 반환
        Notice notice = clubNoticeCommandService.createPureNotice(club, clubMember, request);
        return ClubNoticeResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(clubMember.isStaff())
                .noticeItem(ClubNoticeConverter.toPureNoticeDTO(notice))
                .build();
    }

    @Override
    public void deletePureNotice(Long clubId, String memberId, Long noticeId) {
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 공지 삭제
        clubNoticeCommandService.deletePureNotice(clubId, clubMember, noticeId);
    }

    @Override
    public Long createVote(Long clubId, String memberId, ClubNoticeRequestDTO.CreateClubVoteDTO request) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 투표 생성 및 ID 반환
        Vote vote = clubNoticeCommandService.createVote(club, clubMember, request);
        return vote.getId();
    }

    @Override
    public void deleteVote(Long clubId, String memberId, Long voteId) {
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 투표 삭제
        clubNoticeCommandService.deleteVote(clubId, clubMember, voteId);
    }

    @Override
    public Long haveVote(Long clubId, String memberId, Long voteId, ClubNoticeRequestDTO.VoteResultDTO request) {
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 투표 참여 및 투표 ID 반환
        return clubNoticeCommandService.haveVote(clubId, clubMember, voteId, request);
    }

}
