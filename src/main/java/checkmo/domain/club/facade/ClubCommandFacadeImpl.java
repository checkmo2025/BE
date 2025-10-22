package checkmo.domain.club.facade;

import checkmo.domain.book.entity.Book;
import checkmo.domain.book.facade.BookCommandFacade;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.announcement.Vote;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.service.command.*;
import checkmo.domain.club.service.query.ClubMeetingQueryService;
import checkmo.domain.club.service.query.ClubMemberQueryService;
import checkmo.domain.club.service.query.ClubNoticeQueryService;
import checkmo.domain.club.service.query.ClubQueryService;
import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.global.dto.MemberSharedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubCommandFacadeImpl implements ClubCommandFacade {

    // Domain level 1
    private final BookCommandFacade bookCommandFacade;
    private final BookQueryFacade bookQueryFacade;

    // Domain level 2
    private final MemberQueryFacade memberQueryFacade;

    // 자신의 QueryFacade
    private final ClubQueryFacade clubQueryFacade;

    // 자신의 CommandService
    private final ClubMeetingCommandService clubMeetingCommandService;
    private final ClubManagementCommandService clubManagementCommandService;
    private final ClubBookRecommendCommandService clubBookRecommendCommandService;
    private final ClubNoticeCommandService clubNoticeCommandService;
    private final ClubMembershipCommandService clubMembershipCommandService;

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubNoticeQueryService clubNoticeQueryService;

    /**
     * ClubManagementCommandService
     * 새로운 독서 모임을 생성합니다. (내부용)
     *
     * @param memberId 생성자 회원 ID
     * @param request 모임 생성 요청 정보 DTO
     * @return 생성된 독서 모임의 상세 정보 DTO
     */
    @Override
    public Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request) {
        return clubManagementCommandService.createClub(memberId, request);
    }

    /**
     * ClubManagementCommandService
     * 기존 독서 모임 정보를 수정합니다. (내부용)
     *
     * @param clubId 수정할 모임 ID
     * @param memberId 수정 요청한 회원 ID
     * @param request 모임 수정 요청 정보 DTO
     */
    @Override
    public void updateClub(Long clubId, String memberId, ClubRequestDTO.ClubDetailDTO request) {
        clubManagementCommandService.updateClub(clubId, memberId, request);
    }

    /**
     * ClubMembershipCommandService
     * 독서 모임에 가입을 신청합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 신청자 회원 ID
     * @param request 가입 신청 메시지 DTO
     * @return 가입 신청 후의 모임 정보 DTO
     */
    @Override
    public ClubResponseDTO.ClubInfoDTO joinClub(Long clubId, String memberId, ClubRequestDTO.ClubMemberJoinDTO request) {
        Club club = clubMembershipCommandService.joinClub(clubId, memberId, request);
        return ClubConverter.toClubInfoDTO(club);
    }

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
    @Override
    public ClubResponseDTO.ClubMemberUpdateResponseDTO updateClubMemberStatus(Long clubId, Long targetMemberId, String currentMemberId, String status) {
        ClubMember updatedClubMember = clubMembershipCommandService.updateClubMemberStatus(clubId, targetMemberId, currentMemberId, status);

        // 외부 도메인 정보 조회 및 DTO 변환
        MemberSharedDTO.BasicInfoDTO memberInfo = memberQueryFacade.getMemberBasicInfoForShare(updatedClubMember.getMemberId());
        ClubResponseDTO.ClubMemberDTO updatedClubMemberDTO = ClubConverter.toClubMemberDTO(updatedClubMember, memberInfo);

        // 운영진 여부를 포함해서 반환
        return ClubResponseDTO.ClubMemberUpdateResponseDTO.builder()
                .updatedMember(updatedClubMemberDTO)
                .isRequesterStaff(true) // 이 api 는 운영진만 호출할 수 있으므로 true 로 설정
                .build();
    }

    /**
     * ClubMembershipCommandService
     * 독서 모임에서 탈퇴합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 탈퇴할 회원 ID
     */
    @Override
    public void leaveClub(Long clubId, String memberId) {
        clubMembershipCommandService.leaveClub(clubId, memberId);
    }

    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO createPureNotice(Long clubId, String memberId, ClubRequestDTO.CreateClubNoticeDTO request) {
        // 1. 유효성 검증 (club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 공지 생성 및 DTO 반환
        Notice notice = clubNoticeCommandService.createPureNotice(club, clubMember, request);
        return ClubResponseDTO.ClubNoticeDetailDTO.builder()
                .isStaff(clubMember.isStaff())
                .noticeItem(ClubConverter.toPureNoticeDTO(notice))
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
    public Long createVote(Long clubId, String memberId, ClubRequestDTO.CreateClubVoteDTO request) {
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
    public Long haveVote(Long clubId, String memberId, Long voteId, ClubRequestDTO.VoteResultDTO request) {
        // 1. 유효성 검증(club, clubMember)
        clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 투표 참여 및 투표 ID 반환
        return clubNoticeCommandService.haveVote(clubId, clubMember, voteId, request);
    }

    /**
     * ClubBookRecommendCommandService
     * 모임에 책을 추천합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 추천자 회원 ID
     * @param request 추천 책 정보 DTO
     * @return 추천된 책의 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.BookRecommendDetailDTO recommendBook(Long clubId, String memberId, ClubRequestDTO.CreateBookRecommendDTO request) {
        Long bookRecommendId = clubBookRecommendCommandService.recommendBook(clubId, memberId, request);
        return clubQueryFacade.getRecommendedBookDetail(clubId, bookRecommendId, memberId);
    }

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
    @Override
    public ClubResponseDTO.BookRecommendDetailDTO updateBookRecommend(Long clubId, String memberId, Long bookRecommendId, ClubRequestDTO.UpdateBookRecommendDTO request) {
        Long updateBookRecommendId = clubBookRecommendCommandService.updateBookRecommend(clubId, memberId, bookRecommendId, request);
        return clubQueryFacade.getRecommendedBookDetail(clubId, updateBookRecommendId, memberId);
    }

    /**
     * ClubBookRecommendCommandService
     * 추천한 책을 삭제합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자 회원 ID
     * @param bookRecommendId 삭제할 추천 책 ID
     */
    @Override
    public void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId) {
        clubBookRecommendCommandService.deleteRecommendedBook(clubId, memberId, bookRecommendId);
    }

    @Override
    public Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request) {
        // 1. 유효성 검증(club, clubMember)
        Club club = clubQueryService.validateClub(clubId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(clubId, memberId);

        // 2. 책 저장 후 프록시 객체 가져오기
        bookCommandFacade.saveBook(request.getBookInfo());
        Book proxyBook = bookQueryFacade.findBookReferenceById(request.getBookInfo().getIsbn());

        // 3. 저장할 미팅 생성
        Meeting meeting = ClubConverter.fromMeetingCreateRequestDTOToMeeting(request, proxyBook);
        meeting.setClub(club);

        // 4. 미팅 저장 & 공지사항 자동 생성
        return clubMeetingCommandService.createMeeting(club, clubMember, meeting);
    }

    @Override
    public Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request) {
        // 1. 유효성 검증(meeting, club, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Club club = clubQueryService.validateClub(meeting.getClubId());
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 미팅 수정
        return clubMeetingCommandService.updateMeeting(meeting, club, clubMember, request);
    }

    @Override
    public Long createTopic(String memberId, Long meetingId, BookShelfRequestDTO.TopicDTO request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 생성
        return clubMeetingCommandService.createTopic(meeting, clubMember, request);
    }

    @Override
    public Long updateTopic(String memberId, Long meetingId, Long topicId, BookShelfRequestDTO.TopicDTO request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 수정
        return clubMeetingCommandService.updateTopic(meeting.getClubId(), clubMember, meetingId, topicId, request);
    }

    @Override
    public void deleteTopic(String memberId, Long meetingId, Long topicId) {
        // 1. 유효성 검증 (meeting clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 삭제
        clubMeetingCommandService.deleteTopic(meeting.getClubId(), clubMember, meetingId, topicId);
    }

    @Override
    public MeetingResponseDTO.TopicSelectionDTO selectOrCancelTopic(Long meetingId, Long topicId, MeetingRequestDTO.TopicSelectionDTO request, String memberId) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 선택 or 선택 취소
        Boolean isSelected = clubMeetingCommandService.selectOrCancelTopic(clubMember, meetingId, topicId, request);

        // 3. 응답 DTO 반환
        return ClubConverter.fromParametersToTopicSelectionDTO(topicId, request.getTeamNumber(), isSelected);
    }

    @Override
    public void manageTeams(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 팀 구성
        clubMeetingCommandService.manageTeam(clubMember, meeting, request);
    }

    // TODO: Aspect 로그
    // TODO: Test DB 설정 후, 낙관적 락 동작 테스트
    @Override
    @Retryable( // OptimisticLockingFailureException 발생 시 재시도
            value = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300) // 300ms 간격으로 재시도
    )
    public Long createBookReview(String memberId, Long meetingId, BookShelfRequestDTO.BookReviewDTO request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 한줄평 생성
        return clubMeetingCommandService.createBookReview(clubMember, meeting, request);
    }

    @Override
    @Retryable( // OptimisticLockingFailureException 발생 시 재시도
            value = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300) // 300ms 간격으로 재시도
    )
    public Long updateBookReview(String memberId, Long meetingId, Long reviewId, BookShelfRequestDTO.BookReviewDTO request) {
        // 1. 유효성 검증 (meeting, clubMember, bookReview)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 한줄평 수정
        return clubMeetingCommandService.updateBookReview(clubMember, meeting, reviewId, request);
    }

    @Override
    @Retryable( // OptimisticLockingFailureException 발생 시 재시도
            value = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 300) // 300ms 간격으로 재시도
    )
    public void deleteBookReview(String memberId, Long meetingId, Long reviewId) {
        // 1. 유효성 검증 (meeting, clubMember, bookReview)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 한줄평 삭제
        clubMeetingCommandService.deleteBookReview(clubMember, meeting, reviewId);
    }
}
