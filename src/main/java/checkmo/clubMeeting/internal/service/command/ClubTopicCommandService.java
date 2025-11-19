package checkmo.clubMeeting.internal.service.command;

import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;

/**
 * 독서모임의 '발제'에 대한 비즈니스 요구사항을 수행합니다.
 */
public interface ClubTopicCommandService {

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
    Long createTopic(Long meetingId, String memberId, BookShelfRequestDTO.TopicCreate request);

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
    Long updateTopic(Long meetingId, Long topicId, String memberId, BookShelfRequestDTO.TopicCreate request);

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
    MeetingResponseDTO.TopicSelection selectOrCancelTopic(
            Long meetingId,
            Long topicId,
            String memberId,
            MeetingRequestDTO.TopicSelection request
    );
}
