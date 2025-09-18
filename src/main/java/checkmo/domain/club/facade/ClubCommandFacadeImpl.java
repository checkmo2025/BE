package checkmo.domain.club.facade;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.book.entity.Book;
import checkmo.domain.book.facade.BookCommandFacade;
import checkmo.domain.book.facade.BookQueryFacade;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.entity.meeting.Team;
import checkmo.domain.club.entity.meeting.Topic;
import checkmo.domain.club.service.command.*;
import checkmo.domain.club.service.query.ClubCommunicationQueryService;
import checkmo.domain.club.service.query.ClubMeetingQueryService;
import checkmo.domain.club.service.query.ClubMemberQueryService;
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
    private final ClubCommunicationCommandService clubCommunicationCommandService;
    private final ClubMembershipCommandService clubMembershipCommandService;

    // 자신의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubCommunicationQueryService clubNoticeQueryService;

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

    /**
     * ClubCommunicationCommandService
     * 모임에 공지사항을 작성합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 작성자(운영진) 회원 ID
     * @param request 공지사항 작성 요청 DTO
     * @return 작성된 공지사항의 상세 정보 DTO
     */
    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO createNotice(
            Long clubId, String memberId, ClubRequestDTO.CreateClubNoticeDTO request
    ) {
        // 1. 공지 생성
        Long noticeId = clubCommunicationCommandService.createNotice(clubId, memberId, request);

        // 2. 생성된 공지를 다시 조회
        return clubNoticeQueryService.getNoticeOrVoteDetail(clubId, noticeId, "공지", memberId);
    }

    /**
     * ClubCommunicationCommandService
     * 모임의 공지사항을 삭제합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param noticeId 삭제할 공지사항 ID
     */
    @Override
    public void deleteNotice(Long clubId, String memberId, Long noticeId) {
        clubCommunicationCommandService.deleteNotice(clubId, memberId, noticeId);
    }

    /**
     * ClubCommunicationCommandService
     * 모임에 투표를 생성합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 작성자(운영진) 회원 ID
     * @param request 투표 생성 요청 DTO
     * @return 생성된 투표가 포함된 공지사항 상세 DTO
     */
    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO createVote(Long clubId, String memberId, ClubRequestDTO.CreateClubVoteDTO request) {

        // 1. 투표 생성
        Long voteId = clubCommunicationCommandService.createVote(clubId, memberId, request);

        // 2. 생성된 투표를 다시 조회
        return clubNoticeQueryService.getNoticeOrVoteDetail(clubId, voteId, "투표", memberId);
    }

    /**
     * ClubCommunicationCommandService
     * 모임의 투표를 삭제합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param voteId 삭제할 투표 ID
     */
    @Override
    public void deleteVote(Long clubId, String memberId, Long voteId) {
        clubCommunicationCommandService.deleteVote(clubId, memberId, voteId);
    }

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
    @Override
    public ClubResponseDTO.ClubNoticeDetailDTO participateInPoll(Long clubId, String memberId, Long voteId, ClubRequestDTO.VoteResultDTO request) {

        // 1. 투표 참여
        voteId = clubCommunicationCommandService.participateInPoll(clubId, memberId, voteId, request);

        // 2. 투표 결과를 다시 조회
        return clubNoticeQueryService.getNoticeOrVoteDetail(clubId, voteId, "투표", memberId);
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

        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 책 저장 후 프록시 객체 가져오기
        bookCommandFacade.saveBook(request.getBookInfo());
        Book proxyBook = bookQueryFacade.findBookReferenceById(request.getBookInfo().getIsbn());

        // 3. 저장할 미팅 생성
        Meeting meeting = ClubConverter.fromMeetingCreateRequestDTOToMeeting(request, proxyBook);
        meeting.setClub(club);

        // 4. 미팅 저장 & 공지사항 자동 생성
        return clubMeetingCommandService.createMeeting(club, meeting);
    }

    @Override
    public Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request) {
        // 1. 유효성 검증(meeting, club, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Club club = clubQueryService.validateClub(meeting.getClubId());
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 미팅 수정
        return clubMeetingCommandService.updateMeeting(meeting, club, request);
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
        // 1. 유효성 검증 (meeting, clubMember, topic)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);

        if (!topic.isOwnedBy(clubMember)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        // 2. 발제 수정
        return clubMeetingCommandService.updateTopic(topic, request);
    }

    @Override
    public void deleteTopic(String memberId, Long meetingId, Long topicId) {
        // 1. 유효성 검증 (meeting, clubMember, topic)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);

        if (!topic.isOwnedBy(clubMember)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        // 2. 발제 삭제
        clubMeetingCommandService.deleteTopic(topic);
    }

    @Override
    public MeetingResponseDTO.TopicSelectionDTO selectOrCancelTopic(Long meetingId, Long topicId, MeetingRequestDTO.TopicSelectionDTO request, String memberId) {
        // 1. 유효성 검증 (meeting, clubMember, topic, team)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Team team = clubMeetingQueryService.validateTeam(meetingId, request.getTeamNumber());
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);
        clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 선택 or 선택 취소
        Boolean isSelected = clubMeetingCommandService.selectOrCancelTopic(team, topic, request);

        // 3. 응답 DTO 반환
        return ClubConverter.fromParametersToTopicSelectionDTO(topicId, request.getTeamNumber(), isSelected);
    }

    @Override
    public void manageTeams(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request) {
        // 1. 유효성 검증(meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        if (!clubMember.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 2. 팀 구성
        clubMeetingCommandService.manageTeam(meeting, request);
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
        return clubMeetingCommandService.createBookReview(meeting, clubMember, request);
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
        BookReview bookReview = clubMeetingQueryService.validateBookReview(reviewId, meetingId);

        if (!bookReview.getClubMemberId().equals(clubMember.getId())) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        // 2. 한줄평 수정
        return clubMeetingCommandService.updateBookReview(meeting, bookReview, request);
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
        BookReview bookReview = clubMeetingQueryService.validateBookReview(reviewId, meetingId);

        if (!bookReview.getClubMemberId().equals(clubMember.getId())) {
            throw new GeneralException(ErrorStatus.BOOK_REVIEW_FORBIDDEN);
        }

        // 2. 한줄평 삭제
        clubMeetingCommandService.deleteBookReview(meeting, bookReview);
    }
}
