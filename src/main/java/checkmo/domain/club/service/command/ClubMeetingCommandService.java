package checkmo.domain.club.service.command;

import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;

/**
 * 독서모임의 미팅 생성, 발제 작성, 팀 구성, 독서 후기 등등
 * 독서모임의 토론 전체를 관리
 */
public interface ClubMeetingCommandService {

    /**
     * 독서모임의 미팅을 생성합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 운영진 화면 [모임 생성하기] 클릭시 - 두번째 화면 - 가장 최신 걸로 (와이어프레임 제일 아래)
     *
     * @param clubId 독서모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param request 미팅 생성 요청 DTO
     * @result 생성한 미팅 ID
     *
     * ‼️ 내부에서 공지사항 자동으로 연결해서 생성해주는 로직 반드시 필요
     */
     Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreateRequestDTO request);

    /**
     * 독서모임의 미팅을 수정합니다.
     *
     * 피그마 참고 페이지 : 피그마 페이지 없음
     *
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param request 미팅 수정 요청 DTO
     * @result 수정한 미팅 ID
     *
     * ‼️ 내부에서 공지사항 자동으로 연결해서 기존 공지사항 삭제하고 재생성해주는 로직 반드시 필요
     */
    Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdateRequestDTO request);

    /**
     * 독서모임의 발제를 작성합니다.
     *
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 [발제]추가 클릭시
     *
     * @param memberId 발제 작성자의 ID   -> 해당 독서 클럽에만 포함되면 전부 작성 가능
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param request 발제 내용 DTO
     * @return 생성한 발제 ID
     */
    Long createTopic(Long memberId, Long meetingId, MeetingRequestDTO.TopicDTO request);

    /**
     * 독서모임의 특정 팀이 발제를 선택하고 해제합니다.
     *
     * 피그마 참고 페이지: #독서모임 - 운영진 화면 모임 - 발제 전체보기 클릭시
     *
     * @param memberId 발제 작성자의 ID -> 클럽 회원인지 확인하는 로직
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param request 발제 관리 요청 DTO
     * @return 관리한 발제 ID
     */
    Long toggleTopic(String memberId, Long meetingId, MeetingRequestDTO.TopicManageDTO request);

    /**
     * 독서모임의 특정 팀이 발제를 수정합니다.
     *
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 [발제]추 시
     * @param memberId 발제 작성자의 ID -> 발제 작성자인지 확인하는 용도
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param topicId 발제 ID
     * @param request 수정된 발제 내용 DTO
     * @return 수정한 발제 ID
     */
    Long updateTopic(String memberId, Long meetingId, Long topicId, MeetingRequestDTO.TopicDTO request);

    /**
     * 독서모임의 특정 팀이 발제를 삭제합니다.
     *
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 [발제]추 시
     *
     * @param memberId 발제 작성자의 ID -> 발제 작성자인지 확인하는 용도
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param topicId 발제 ID
     */
    void deleteTopic(String memberId, Long meetingId, Long topicId);

    /**
     * 독서모임의 팀을 구성합니다.
     *
     * 피그마 참고 페이지: #독서모임 - 운영진 화면 [조 관리하기] 클릭시
     *
     * @param memberId 팀 구성자의 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param request 팀 구성 요청 DTO
     * @return 해당 미팅의 ID
     */
    Long manageTeam(String memberId, Long meetingId, MeetingRequestDTO.TeamManageDTO request);

    /**
     * 독서모임의 한줄평을 작성합니다.
     *
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 추가 하기
     *
     * @param memberId 한줄평 작성자의 ID -> 독서 토론 참여한 사람만 작성 가능 -> Meeting 참여자 조회해서 비교
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param request 한줄평 내용 DTO (내용 + 평점)
     * @return 생성한 한줄평 ID
     */
    Long createBookReview(String memberId, Long meetingId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * 독서모임의 한줄평을 수정합니다.
     *
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 등록시
     *
     * @param memberId 한줄평 작성자의 ID -> 독서 토론 참여한 사람만 작성 가능 -> Meeting 참여자 조회해서 비교
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param reviewId 한줄평 ID
     * @param request 한줄평 내용 DTO (내용 + 평점)
     */
    Long updateBookReview(String memberId, Long meetingId, Long reviewId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * 독서모임의 한줄평을 삭제합니다.
     *
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 등록시
     *
     * @param memberId 한줄평 작성자의 ID -> 독서 토론 참여한 사람만 작성 가능 -> Meeting 참여자 조회해서 비교
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param reviewId 한줄평 ID
     */
    void deleteBookReview(String memberId, Long meetingId, Long reviewId);

}
