package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.TopicRepository;
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
    public Topic validateTopic(Long topicId, Long meetingId) throws ClubMeetingException {
        return topicRepository.findByIdAndMeetingId(topicId, meetingId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.TOPIC_NOT_FOUND));
    }

}
