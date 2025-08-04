package checkmo.domain.club.service.query;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.converter.ClubConverter;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.entity.meeting.TeamTopic;
import checkmo.domain.club.entity.meeting.Topic;
import checkmo.domain.club.repository.meeting.BookReviewRepository;
import checkmo.domain.club.repository.meeting.MeetingRepository;
import checkmo.domain.club.repository.meeting.TeamTopicRepository;
import checkmo.domain.club.repository.meeting.TopicRepository;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMeetingQueryServiceImpl implements ClubMeetingQueryService {
    private final MeetingRepository meetingRepository;
    private final BookReviewRepository bookReviewRepository;
    private final TopicRepository topicRepository;
    private final TeamTopicRepository teamTopicRepository;

    private final ClubMemberQueryService clubMemberQueryService;
    private final ClubQueryService clubQueryService;

    @Override
    public MeetingResponseDTO.MeetingDetailDTO findMeetingById(Long meetingId) {
        return null;
    }

    @Override
    public List<Meeting> findMeetingsByClubAndCursor(Long clubId, Long cursorId, Integer size) {
        return meetingRepository.findMeetingsByClubIdAndCursorDesc(clubId, cursorId, size + 1);
    }

    @Override
    public List<Topic> findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId) {
        return topicRepository.findTopicsByCursorAsc(meetingId, cursorId, size + 1);
    }

    @Override
    public Map<Long, List<Integer>> findTeamTopicsWithTeamByTopicIds(List<Long> topicIds) {
        List<TeamTopic> teamTopics = teamTopicRepository.findTeamTopicsWithTeamByTopicIds(topicIds);
        return teamTopics.stream()
                .collect(Collectors.groupingBy(
                        TeamTopic::getTopicId, //key: 토픽 ID(토픽 ID로 그룹화)
                        Collectors.mapping(tt -> tt.getTeam().getTeamNumber(), Collectors.toList()) //value: 해당 토픽을 선택한 팀 번호 리스트(같은 그룹에 속하는 TeamTopic의 팀 번호 List 생성)
                ));
    }

    @Override
    public MeetingResponseDTO.TeamTopicDTO findTeamsByMeeting(Long meetingId, Integer teamNumber) {
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

        return meetingRepository.findMeetingsByClubIdAndGenerationAndCursorDesc(clubId, generation, cursorId, size + 1);
    }

    @Override
    public List<MeetingResponseDTO.MeetingInfoDTO> getClubMeetingByYearAndMonth(Long clubId, int year, int month, String memberId) {
        Club club = clubQueryService.validateClub(clubId);
        clubMemberQueryService.validateClubMember(clubId, memberId);

        LocalDateTime startDateTime = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusMonths(1); //12월의 경우 다음 해 1월로 넘어감

        List<Meeting> meetings = meetingRepository.findByClubIdAndMeetingTimeBetweenAsc(clubId, startDateTime, endDateTime);

        return ClubConverter.fromMeetingListToMeetingInfoDTOList(meetings);
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
