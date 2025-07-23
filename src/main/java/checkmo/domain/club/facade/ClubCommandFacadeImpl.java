package checkmo.domain.club.facade;

import checkmo.domain.club.service.command.ClubManagementCommandService;
import checkmo.domain.club.service.command.ClubMeetingCommandService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubCommandFacadeImpl implements ClubCommandFacade {

    private final ClubMeetingCommandService clubMeetingCommandService;
    private final ClubManagementCommandService clubManagementCommandService;

    /**
     * ClubManagementCommandService
     * 새로운 독서 모임을 생성합니다. (내부용)
     *
     * @param memberId  생성자 회원 ID
     * @param request   모임 생성 요청 정보 DTO
     * @return 생성된 독서 모임의 상세 정보 DTO
     */
    @Override
    public Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request) {
        return clubManagementCommandService.createClub(memberId, request);
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
    public Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request) {
        return clubMeetingCommandService.createMeeting(clubId, memberId, request);
    }

    @Override
    public Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request) {
        return clubMeetingCommandService.updateMeeting(meetingId, memberId, request);
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
        return 0L;
    }

    @Override
    public Long updateBookReview(String memberId, Long meetingId, Long reviewId, BookShelfRequestDTO.BookReviewDTO request) {
        return 0L;
    }

    @Override
    public void deleteBookReview(String memberId, Long meetingId, Long reviewId) {

    }
}
