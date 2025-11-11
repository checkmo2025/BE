package checkmo.clubMeeting.internal.service.command;

import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;

/**
 * 독서모임의 미팅 생성, 발제 작성, 팀 구성, 독서 후기 등등 독서모임의 토론 전체를 관리
 */
public interface ClubMeetingCommandService {

    /**
     * 독서모임의 미팅을 생성합니다.
     * <p>
     * 피그마 참고 페이지 : #독서모임 - 운영진 화면 [모임 생성하기] 클릭시 - 두번째 화면 - 가장 최신 걸로 (와이어프레임 제일 아래)
     *
     * @param clubId   미팅을 생성할 독서모임 ID
     * @param memberId 생성자(운영진) 회원 ID
     * @param request  미팅 생성 요청 DTO
     * @return 생성한 미팅 ID
     * <p>
     * ‼️ 내부에서 공지사항 자동으로 연결해서 생성해주는 로직 반드시 필요
     */
    Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request);

    /**
     * 독서모임의 미팅을 수정합니다.
     * <p>
     * 피그마 참고 페이지 : 피그마 페이지 없음
     *
     * @param meetingId 수정할 미팅 ID
     * @param memberId  요청자(운영진) 회원 ID
     * @param request   미팅 수정 요청 DTO
     * @return 수정한 미팅 ID
     * <p>
     * ‼️ 내부에서 공지사항 자동으로 연결해서 기존 공지사항 삭제하고 재생성해주는 로직 반드시 필요
     */
    Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request);

    /**
     * 독서모임의 발제를 작성합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 [발제]추가 클릭시
     *
     * @param meetingId 미팅 ID
     * @param memberId  작성자 회원 ID
     * @param request   발제 내용 DTO
     * @return 생성한 발제 ID
     */
    Long createTopic(Long meetingId, String memberId, BookShelfRequestDTO.TopicDTO request);

    /**
     * 독서모임의 특정 팀이 발제를 수정합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 [발제]추 시
     *
     * @param meetingId 미팅 ID
     * @param topicId   발제 ID
     * @param memberId  요청자 회원 ID
     * @param request   수정된 발제 내용 DTO
     * @return 수정한 발제 ID
     */
    Long updateTopic(Long meetingId, Long topicId, String memberId, BookShelfRequestDTO.TopicDTO request);

    /**
     * 독서모임의 특정 팀이 발제를 삭제합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 [발제]추 시
     *
     * @param meetingId 미팅 ID
     * @param topicId   발제 ID
     * @param memberId  요청자 회원 ID
     */
    void deleteTopic(Long meetingId, Long topicId, String memberId);

    /**
     * 독서모임의 특정 팀이 발제를 선택하고 해제합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임 - 운영진 화면 모임 - 발제 전체보기 클릭시
     *
     * @param memberId  요청자 회원 ID
     * @param meetingId 미팅 ID
     * @param topicId   발제 ID
     * @param request   발제 선택 여부 (true: 선택, false: 해제)
     */
    MeetingResponseDTO.TopicSelectionDTO selectOrCancelTopic(Long meetingId, Long topicId, String memberId,
                                                             MeetingRequestDTO.TopicSelectionDTO request);

    /**
     * 독서모임의 팀을 구성합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임 - 운영진 화면 [조 관리하기] 클릭시
     *
     * @param meetingId 미팅 ID 팀 구성 요청자
     * @param memberId  요청자(운영진) 회원 ID
     * @param request   팀 구성 요청 DTO
     */
    void manageTeam(Long meetingId, String memberId, MeetingRequestDTO.TeamManageDTO request);

    /**
     * 독서모임의 한줄평을 작성합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 추가 하기
     *
     * @param meetingId 미팅 ID
     * @param memberId  작성자 회원 ID
     * @param request   한줄평 내용 DTO (내용 + 평점)
     * @return 생성한 한줄평 ID
     */
    Long createBookReview(Long meetingId, String memberId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * 독서모임의 한줄평을 수정합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 등록시
     *
     * @param meetingId 미팅 ID
     * @param reviewId  한줄평 ID
     * @param memberId  요청자 회원 ID
     * @param request   한줄평 내용 DTO (내용 + 평점)
     */
    Long updateBookReview(Long meetingId, Long reviewId, String memberId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * 독서모임의 한줄평을 삭제합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 등록시
     *
     * @param meetingId 미팅 ID
     * @param reviewId  한줄평
     * @param memberId  요청자 회원 ID
     */
    void deleteBookReview(Long meetingId, Long reviewId, String memberId);

}
