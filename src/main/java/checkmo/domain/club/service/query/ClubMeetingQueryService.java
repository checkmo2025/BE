package checkmo.domain.club.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.entity.meeting.Topic;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;

import java.util.List;

/**
 * 독서 모임의 토론 조회 서비스
 *
 * 독서 모임 토론에 대한 조회 기능을 담당
 * ex) 토론 일정 조회, 토론 참석자 목록 조회, 한줄평(책장) 등등 조회 등을 처리
 */
public interface ClubMeetingQueryService {

    /**
     * 독서 모임의 특정 미팅을 조회합니다. -
     *
     * 피그마 참고 페이지 : #독서모임 - 운영진 화면 - 등록 완료시 나오는 화면 - 등록된 모임 상세보기
     *
     * @param meetingId 미팅 ID
     * @return 조회한 미팅 정보 DTO -> 전체 발제는 최대 4개, 각 조별 발제들 최대 4개 (전부 최신 내림차순 정렬)
     */
    MeetingResponseDTO.MeetingDetailDTO findMeetingById(Long meetingId);

    /**
     * 독서 모임의 모든 미팅을 조회합니다. (책장 아님) - 이 페이지에서는 발제 작성 불가 및, 한줄평 조회 불가
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 생성 후, 모임 리스트
     *
     * @param clubId 독서 모임 ID
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null)
     * @param size 조회할 미팅 개수
     * @return 조회한 미팅 리스트
     */
    List<Meeting> findMeetingsByClubAndCursor(Long clubId, Long cursorId, Integer size);

    /**
     * 특정 미팅의 전체 토픽을 조회합니다.
     *
     * 피그마 참고 페이지 : #독서모임(사용자) - 책장 [특정 책]에서 [발제] 클릭시
     *
     * @param meetingId 미팅 ID
     * @return 조회한 토픽 정보 DTO
     */
    List<Topic> findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId);

    /**
     * 독서 모임의 미팅의 팀별 인원 조회 (해당 팀이 선택한 발제 리스트도 포함)
     *
     * 피그마 참고 페이지 : #독서모임 - 운영진 화면 모임 - 특정 조 전체보기 클릭시
     *
     * @param meetingId 미팅 ID
     * @param teamNumber 팀 번호 (1, 2, 3, 4 팀 -> 실제로 프론트에서는 A, B, C, D로 표시)
     * @return 조회한 팀 정보 DTO
     */
    MeetingResponseDTO.TeamTopicDTO findTeamsByMeeting(Long meetingId, Integer teamNumber);

    /**
     * 독서 모임의 책장(한줄평) 리스트를 조회합니다.
     *
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책] 클릭시
     *
     * @param meetingId 미팅 ID
     * @param lastReviewId 마지막으로 조회한 한줄평 ID (무한 스크롤용, 처음에는 null 또는 0)
     * @param size 조회할 한줄평 개수
     * @return 조회한 한줄평 리스트
     */
    List<BookReview> findBookReviewsByMeeting(Long meetingId, Long lastReviewId, int size);

    /**
     * 독서모임의 모임 캘린더를 조회합니다.
     *
     * @param clubId 독서클럽 ID
     * @param year 조회하고자 하는 연도
     * @param month 조회하고자 하는 달
     * @param memberId 요청자 회원 ID
     * @return 독서모임의 모임 정보 리스트
     */
    List<MeetingResponseDTO.MeetingInfoDTO> getClubMeetingByYearAndMonth(Long clubId, int year, int month, String memberId);

    /**
     * 모임을 책장 리스트로 조회
     *
     * 피그마 참고 페이지 : #독서모임(사용자) - 책장 홈화면
     *
     * @param clubId 독서 클럽 ID
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return Meeting 리스트
     */
    List<Meeting> getBookShelfList(Long clubId, Integer generation, Long cursorId, Integer size, String memberId);

    /**
     * 독서모임이 존재하는지 확인합니다.
     *
     * @param meetingId 미팅 ID
     * @return Meeting 존재하는 미팅 객체
     * @throws GeneralException 미팅이 존재하지 않을 경우
     */
    Meeting validateMeeting(Long meetingId) throws GeneralException;

    /**
     * 독서모임의 발제가 존재하는지 확인합니다.
     *
     * @param topicId 발제 ID
     * @param meetingId 미팅 ID
     * @return Topic 존재하는 발제 객체
     */
    Topic validateTopic(Long topicId, Long meetingId) throws GeneralException;
}
