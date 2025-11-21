package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;

/**
 * 독서 모임의 발제 관련 조회 서비스
 */
public interface ClubTopicQueryService {
    /**
     * 특정 미팅의 토픽을 커서 기반 조회합니다.
     *
     * @param meetingId 미팅 ID
     * @param cursorId  커서 ID (페이징을 위한 커서, 처음에는 null)
     * @param size      조회할 토픽 개수 (null이면 전체 조회)
     * @return 조회한 토픽 정보 DTO
     */
    List<Topic> findTopicsByMeeting(Long meetingId, Long cursorId, Integer size);

    /**
     * 독서모임의 발제가 존재하는지 확인합니다.
     *
     * @param topicId   발제 ID
     * @param meetingId 미팅 ID
     * @return Topic 존재하는 발제 객체
     */
    Topic validateTopic(Long topicId, Long meetingId) throws GeneralException;

}
