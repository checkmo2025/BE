package checkmo.domain.club.facade.impl;

import checkmo.domain.club.facade.ClubCommandFacade;
import checkmo.domain.club.service.command.*;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClubCommandFacadeImpl implements ClubCommandFacade {

    private final ClubManagementCommandService clubManagementCommandService;
    private final ClubQueryService clubQueryService;

    @Override
    public ClubResponseDTO.ClubDetailDTO createClub(String memberId, ClubRequestDTO.ClubDetailDTO request) {
        // 1. 클럽 생성
        Long clubId = clubManagementCommandService.createClub(memberId, request);

        // 2. 생성된 클럽 상세 정보 조회 및 반환
        return clubQueryService.getClubInfo(clubId, memberId);
    }

    @Override
    public ClubResponseDTO.ClubInfoDTO joinClub(Long clubId, String memberId, ClubRequestDTO.ClubMemberJoinDTO request) {
        return null;
    }

    @Override
    public void approveJoinRequest(Long clubId, String memberId, Long clubMemberId) {
    }

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO createNotice(Long clubId, String memberId, ClubRequestDTO.CreateClubNoticeDTO request) {
        return null;
    }

    @Override
    public void deleteNotice(Long clubId, String memberId, Long noticeId) {
    }

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO createVote(Long clubId, String memberId, ClubRequestDTO.CreateClubVoteDTO request) {
        return null;
    }

    @Override
    public void deleteVote(Long clubId, String memberId, Long voteId) {
    }

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO participateInPoll(Long clubId, String memberId, Long voteId, ClubRequestDTO.VoteResultDTO request) {
        return null;
    }

    @Override
    public ClubResponseDTO.BookRecommendDetailDTO recommendBook(Long clubId, String memberId, ClubRequestDTO.CreateBookRecommendDTO request) {
        return null;
    }

    @Override
    public ClubResponseDTO.BookRecommendDetailDTO updateBookRecommend(Long clubId, String memberId, Long bookRecommendId, ClubRequestDTO.UpdateBookRecommendDTO request) {
        return null;
    }

    @Override
    public void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId) {
    }

    @Override
    public MeetingResponseDTO.InProgressMeetingDetailDTO createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request) {
        return null;
    }

    @Override
    public MeetingResponseDTO.InProgressMeetingDetailDTO updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request) {
        return null;
    }

    @Override
    public MeetingResponseDTO.TopicDTO createTopic(Long memberId, Long meetingId, MeetingRequestDTO.TopicDTO request) {
        return null;
    }

    @Override
    public MeetingResponseDTO.TopicDTO updateTopic(String memberId, Long meetingId, Long topicId, MeetingRequestDTO.TopicDTO request) {
        return null;
    }

    @Override
    public void deleteTopic(String memberId, Long meetingId, Long topicId) {
    }

    @Override
    public void toggleTopic(String memberId, Long meetingId, MeetingRequestDTO.TopicManageDTO request) {
    }

    @Override
    public void manageTeam(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request) {
    }

    @Override
    public Long createBookReview(String memberId, Long meetingId, BookShelfRequestDTO.BookReviewDTO request) {
        return null;
    }

    @Override
    public Long updateBookReview(String memberId, Long meetingId, Long reviewId, BookShelfRequestDTO.BookReviewDTO request) {
        return null;
    }

    @Override
    public void deleteBookReview(String memberId, Long meetingId, Long reviewId) {
    }
}
