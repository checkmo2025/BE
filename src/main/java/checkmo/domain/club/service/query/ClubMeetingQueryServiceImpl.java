package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.entity.meeting.Topic;
import checkmo.domain.club.repository.meeting.BookReviewRepository;
import checkmo.domain.club.repository.meeting.MeetingRepository;
import checkmo.domain.club.repository.meeting.TopicRepository;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMeetingQueryServiceImpl implements ClubMeetingQueryService {
    private final MeetingRepository meetingRepository;
    private final BookReviewRepository bookReviewRepository;
    private final TopicRepository topicRepository;

    private final ClubMemberQueryService clubMemberQueryService;

    @Override
    public MeetingResponseDTO.InProgressMeetingDetailDTO findMeetingById(Long meetingId) {
        return null;
    }

    @Override
    public List<Meeting> findMeetingsByClubAndCursor(Long clubId, Long cursorId, Integer size) {
        return meetingRepository.findMeetingsByClubIdAndCursorDesc(clubId, cursorId, size + 1);
    }

    @Override
    public List<Topic> findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId) {
        Meeting meeting = validateMeeting(meetingId);
        clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        return topicRepository.findTopicsByCursorAsc(meetingId, cursorId, size + 1);
    }

    @Override
    public MeetingResponseDTO.TeamDTO findTeamsByMeeting(Long meetingId, Integer teamNumber) {
        return null;
    }

    @Override
    public List<BookReview> findBookReviewsByMeeting(Long meetingId, Long lastReviewId, int size) {
        return bookReviewRepository.findBookReviewsByCusor(meetingId, lastReviewId, size + 1);
    }

    @Override
    public Meeting validateMeeting(Long meetingId) throws GeneralException {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEETING_NOT_FOUND));
    }

    @Override
    public Topic validateTopic(Long topicId, Long meetingId) throws GeneralException {
        return topicRepository.findByIdAndMeetingId(topicId, meetingId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOPIC_NOT_FOUND));
    }
}
