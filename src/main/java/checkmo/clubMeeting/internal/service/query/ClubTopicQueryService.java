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
public class ClubTopicQueryService {

    private final TopicRepository topicRepository;

    public List<Topic> retrieveTopics(Long meetingId, Long cursorId, Integer size) {
        return topicRepository.findAllByMeetingIdAndCursorOrderByIdDesc(meetingId, cursorId, size);
    }

    public Topic validateTopic(Long topicId, Long meetingId) throws ClubMeetingException {
        return topicRepository.findByIdAndMeetingId(topicId, meetingId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.TOPIC_NOT_FOUND));
    }
}
