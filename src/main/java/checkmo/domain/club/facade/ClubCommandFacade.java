package checkmo.domain.club.facade;

import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;

/**
 * Club Domain Command Facade
 *
 * Club 도메인의 모든 Command(생성, 수정, 삭제) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface ClubCommandFacade {

    /**
     * ClubManagementCommandService
     * 새로운 독서 모임을 생성합니다. (내부용)
     *
     * @param memberId 생성자 회원 ID
     * @param request 모임 생성 요청 정보 DTO
     * @return 생성된 독서 모임의 상세 정보 DTO
     */
    Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request); //

    /**
     * ClubManagementCommandService
     * 기존 독서 모임 정보를 수정합니다. (내부용)
     *
     * @param clubId 수정할 모임 ID
     * @param memberId 수정 요청한 회원 ID
     * @param request 모임 수정 요청 정보 DTO
     */
    void updateClub(Long clubId, String memberId, ClubRequestDTO.ClubDetailDTO request);

    /**
     * ClubMembershipCommandService
     * 독서 모임에 가입을 신청합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 신청자 회원 ID
     * @param request 가입 신청 메시지 DTO
     * @return 가입 신청 후의 모임 정보 DTO
     */
    ClubResponseDTO.ClubInfoDTO joinClub(Long clubId, String memberId, ClubRequestDTO.ClubMemberJoinDTO request); //

    /**
     * ClubMembershipCommandService
     * 독서 모임 회원의 등급(상태/역할)을 수정합니다. (내부용)
     *
     * @param clubId 독서 모임 ID
     * @param targetMemberId 수정 대상 회원 ID
     * @param currentMemberId 요청자(운영진) 회원 ID
     * @param status 수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
     * @return 수정된 회원의 응답 DTO
     */
    ClubResponseDTO.ClubMemberUpdateResponseDTO updateClubMemberStatus(Long clubId, Long targetMemberId, String currentMemberId, String status);

    /**
     * ClubMembershipCommandService
     * 독서 모임에서 탈퇴합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 탈퇴할 회원 ID
     */
    void leaveClub(Long clubId, String memberId);

    /**
     * ClubCommunicationCommandService
     * 모임에 공지사항을 작성합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 작성자(운영진) 회원 ID
     * @param request 공지사항 작성 요청 DTO
     * @return 생성된 공지사항 id
     */
    ClubResponseDTO.ClubNoticeDetailDTO createPureNotice(Long clubId, String memberId, ClubRequestDTO.CreateClubNoticeDTO request); //

    /**
     * ClubCommunicationCommandService
     * 모임의 공지사항을 삭제합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param noticeId 삭제할 공지사항 ID
     */
    void deletePureNotice(Long clubId, String memberId, Long noticeId); //

    /**
     * ClubCommunicationCommandService
     * 모임에 투표를 생성합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 작성자(운영진) 회원 ID
     * @param request 투표 생성 요청 DTO
     * @return 생성된 투표 ID
     */
    ClubResponseDTO.ClubNoticeDetailDTO createVote(Long clubId, String memberId, ClubRequestDTO.CreateClubVoteDTO request); //

    /**
     * ClubCommunicationCommandService
     * 모임의 투표를 삭제합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param voteId 삭제할 투표 ID
     */
    void deleteVote(Long clubId, String memberId, Long voteId); //

    /**
     * ClubCommunicationCommandService
     * 모임의 투표에 참여합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 참여자 회원 ID
     * @param voteId 투표 ID
     * @param request 투표 선택 항목 DTO
     * @return 참여 결과가 반영된 투표 상세 DTO
     */
    ClubResponseDTO.ClubNoticeDetailDTO haveVote(Long clubId, String memberId, Long voteId, ClubRequestDTO.VoteResultDTO request);

    /**
     * ClubBookRecommendCommandService
     * 모임에 책을 추천합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 추천자 회원 ID
     * @param request 추천 책 정보 DTO
     * @return 추천된 책의 상세 정보 DTO
     */
    ClubResponseDTO.BookRecommendDetailDTO recommendBook(Long clubId, String memberId, ClubRequestDTO.CreateBookRecommendDTO request);

    /**
     * ClubBookRecommendCommandService
     * 추천한 책 정보를 수정합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자 회원 ID
     * @param bookRecommendId 수정할 추천 책 ID
     * @param request 수정할 정보 DTO
     * @return 수정된 책의 상세 정보 DTO
     */
    ClubResponseDTO.BookRecommendDetailDTO updateBookRecommend(Long clubId, String memberId, Long bookRecommendId, ClubRequestDTO.UpdateBookRecommendDTO request);

    /**
     * ClubBookRecommendCommandService
     * 추천한 책을 삭제합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자 회원 ID
     * @param bookRecommendId 삭제할 추천 책 ID
     */
    void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId);

    /**
     * ClubMeetingCommandService
     * 모임의 미팅(토론)을 생성합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 생성자(운영진) 회원 ID
     * @param request 미팅 생성 요청 DTO
     * @return 생성된 미팅의 상세 정보 DTO
     */
    Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request);

    /**
     * ClubMeetingCommandService
     * 모임의 미팅(토론) 정보를 수정합니다. (내부용)
     *
     * @param meetingId 수정할 미팅 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param request 미팅 수정 요청 DTO
     * @return 수정된 미팅의 상세 정보 DTO
     */
    Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request);

    /**
     * ClubMeetingCommandService
     * 미팅의 발제(토픽)를 작성합니다. (내부용)
     *
     * @param memberId 작성자 회원 ID
     * @param meetingId 미팅 ID
     * @param request 발제 내용 DTO
     * @return 생성된 발제 ID
     */
    Long createTopic(String memberId, Long meetingId, BookShelfRequestDTO.TopicDTO request);

    /**
     * ClubMeetingCommandService
     * 미팅의 발제(토픽)를 수정합니다. (내부용)
     *
     * @param memberId 요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param topicId 수정할 발제 ID
     * @param request 수정할 발제 내용 DTO
     * @return 수정된 발제 ID
     */
    Long updateTopic(String memberId, Long meetingId, Long topicId, BookShelfRequestDTO.TopicDTO request);

    /**
     * ClubMeetingCommandService
     * 미팅의 발제(토픽)를 삭제합니다. (내부용)
     *
     * @param memberId 요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param topicId 삭제할 발제 ID
     */
    void deleteTopic(String memberId, Long meetingId, Long topicId);

    /**
     * ClubMeetingCommandService
     * 특정 팀이 발제를 선택/해제(토글)합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @param topicId 발제 ID
     * @param request 발제 선택/해제 요청 DTO
     * @param memberId 요청자 회원 ID
     * @return 발제 선택 결과 DTO
     */
    MeetingResponseDTO.TopicSelectionDTO selectOrCancelTopic(Long meetingId, Long topicId, MeetingRequestDTO.TopicSelectionDTO request, String memberId);

    /**
     * ClubMeetingCommandService
     * 미팅의 팀을 구성(관리)합니다. (내부용)
     *
     * @param memberId 요청자(운영진) 회원 ID
     * @param meetingId 미팅 ID
     * @param request 팀 구성 요청 DTO
     */
    void manageTeams(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request);

    /**
     * ClubMeetingCommandService
     * 미팅의 한줄평을 작성합니다. (내부용)
     *
     * @param memberId 작성자 회원 ID
     * @param meetingId 미팅 ID
     * @param request 한줄평 내용 및 평점 DTO
     * @return 생성된 한줄평 정보 DTO
     */
    Long createBookReview(String memberId, Long meetingId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * ClubMeetingCommandService
     * 작성한 한줄평을 수정합니다. (내부용)
     *
     * @param memberId 요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param reviewId 수정할 한줄평 ID
     * @param request 수정할 한줄평 내용 및 평점 DTO
     * @return 수정된 한줄평의 ID
     */
    Long updateBookReview(String memberId, Long meetingId, Long reviewId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * ClubMeetingCommandService
     * 작성한 한줄평을 삭제합니다. (내부용)
     *
     * @param memberId 요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param reviewId 삭제할 한줄평 ID
     */
    void deleteBookReview(String memberId, Long meetingId, Long reviewId);
}