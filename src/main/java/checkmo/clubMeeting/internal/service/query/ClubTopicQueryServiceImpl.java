package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubTopicQueryServiceImpl implements ClubTopicQueryService {

    private final TopicRepository topicRepository;

    @Override
    public List<Topic> findTopicsByMeeting(Long meetingId, Long cursorId, Integer size) {
        return topicRepository.findAllByCursorOrderByIdDesc(meetingId, cursorId, size);
    }

    @Override
    public Topic validateTopic(Long topicId, Long meetingId) throws GeneralException {
        return topicRepository.findByIdAndMeetingId(topicId, meetingId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
    }

}
