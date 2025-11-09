package checkmo.clubMeeting.facade;

import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;

/**
 * Club Meeting Domain Command Facade
 * <p>
 * Club 미팅 관련 모든 Command(미팅 생성/수정, 발제 생성/수정/삭제/선택, 팀 구성, 한줄평 생성/수정/삭제 ) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface ClubMeetingCommandFacade {

    /**
     * ClubMeetingCommandService 모임의 미팅(토론)을 생성합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 생성자(운영진) 회원 ID
     * @param request  미팅 생성 요청 DTO
     * @return 생성된 미팅의 상세 정보 DTO
     */
    Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request);

    /**
     * ClubMeetingCommandService 모임의 미팅(토론) 정보를 수정합니다. (내부용)
     *
     * @param meetingId 수정할 미팅 ID
     * @param memberId  요청자(운영진) 회원 ID
     * @param request   미팅 수정 요청 DTO
     * @return 수정된 미팅의 상세 정보 DTO
     */
    Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request);

    /**
     * ClubMeetingCommandService 미팅의 발제(토픽)를 작성합니다. (내부용)
     *
     * @param memberId  작성자 회원 ID
     * @param meetingId 미팅 ID
     * @param request   발제 내용 DTO
     * @return 생성된 발제 ID
     */
    Long createTopic(String memberId, Long meetingId, BookShelfRequestDTO.TopicDTO request);

    /**
     * ClubMeetingCommandService 미팅의 발제(토픽)를 수정합니다. (내부용)
     *
     * @param memberId  요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param topicId   수정할 발제 ID
     * @param request   수정할 발제 내용 DTO
     * @return 수정된 발제 ID
     */
    Long updateTopic(String memberId, Long meetingId, Long topicId, BookShelfRequestDTO.TopicDTO request);

    /**
     * ClubMeetingCommandService 미팅의 발제(토픽)를 삭제합니다. (내부용)
     *
     * @param memberId  요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param topicId   삭제할 발제 ID
     */
    void deleteTopic(String memberId, Long meetingId, Long topicId);

    /**
     * ClubMeetingCommandService 특정 팀이 발제를 선택/해제(토글)합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @param topicId   발제 ID
     * @param request   발제 선택/해제 요청 DTO
     * @param memberId  요청자 회원 ID
     * @return 발제 선택 결과 DTO
     */
    MeetingResponseDTO.TopicSelectionDTO selectOrCancelTopic(Long meetingId, Long topicId,
                                                             MeetingRequestDTO.TopicSelectionDTO request,
                                                             String memberId);

    /**
     * ClubMeetingCommandService 미팅의 팀을 구성(관리)합니다. (내부용)
     *
     * @param memberId  요청자(운영진) 회원 ID
     * @param meetingId 미팅 ID
     * @param request   팀 구성 요청 DTO
     */
    void manageTeams(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request);

    /**
     * ClubMeetingCommandService 미팅의 한줄평을 작성합니다. (내부용)
     *
     * @param memberId  작성자 회원 ID
     * @param meetingId 미팅 ID
     * @param request   한줄평 내용 및 평점 DTO
     * @return 생성된 한줄평 정보 DTO
     */
    Long createBookReview(String memberId, Long meetingId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * ClubMeetingCommandService 작성한 한줄평을 수정합니다. (내부용)
     *
     * @param memberId  요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param reviewId  수정할 한줄평 ID
     * @param request   수정할 한줄평 내용 및 평점 DTO
     * @return 수정된 한줄평의 ID
     */
    Long updateBookReview(String memberId, Long meetingId, Long reviewId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * ClubMeetingCommandService 작성한 한줄평을 삭제합니다. (내부용)
     *
     * @param memberId  요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param reviewId  삭제할 한줄평 ID
     */
    void deleteBookReview(String memberId, Long meetingId, Long reviewId);
}