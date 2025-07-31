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
    private final ClubQueryService clubQueryService;

    @Override
    public MeetingResponseDTO.InProgressMeetingDetailDTO findMeetingById(Long meetingId) {
        return null;
    }

    @Override
    public MeetingResponseDTO.MeetingListDTO findAllMeetingsByClub(Long clubId, Long cursorId) {
        return null;
    }

    @Override
    public List<Topic> findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId) {
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
    public List<Meeting> getBookShelfList(Long clubId, Integer generation, Long cursorId, Integer size, String memberId) {
        clubQueryService.validateClub(clubId);
        clubMemberQueryService.validateClubMember(clubId, memberId);

        return meetingRepository.findMeetingsByClubIdAndGenerationAndCursorDesc(clubId, generation, cursorId, size);
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
