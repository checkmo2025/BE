package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubTopicQueryService {

    private final TopicRepository topicRepository;

    public List<Topic> retrieveTopics(Long meetingId) {
        return topicRepository.findAllByMeetingIdOrderByIdDesc(meetingId);
    }

    public List<Topic> retrieveTopics(Long meetingId, Long cursorId, int size) {
        return topicRepository.findByMeetingIdWithCursor(meetingId, cursorId, PageRequest.of(0, size));
    }

    public Topic validateTopic(Long topicId) throws ClubMeetingException {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.TOPIC_NOT_FOUND));
    }

    public Topic validateTopic(Long topicId, Long meetingId) throws ClubMeetingException {
        return topicRepository.findByIdAndMeetingId(topicId, meetingId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.TOPIC_NOT_FOUND));
    }
}
