package checkmo.clubMeeting.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingTeamQueryService;
import checkmo.clubMeeting.internal.service.query.ClubTopicQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.TopicCreate;
import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubTopicCommandServiceImpl implements ClubTopicCommandService {

    private final ClubManagementAPI clubManagementAPI;

    // 자신의 QueryService
    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubTopicQueryService clubTopicQueryService;
    private final ClubMeetingTeamQueryService clubMeetingTeamQueryService;

    // 자신의 Repository
    private final TopicRepository topicRepository;
    private final TeamTopicRepository teamTopicRepository;

    @Override
    public Long createTopic(Long meetingId, String memberId, TopicCreate request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        // 2. 발제 생성
        checkmo.clubMeeting.internal.entity.Topic topic = ClubMeetingConverter.fromTopicDTOToTopic(request, clubMemberId);
        topic.setMeeting(meeting);

        // 3. 발제 저장
        return topicRepository.save(topic).getId();
    }

    @Override
    public Long updateTopic(Long meetingId, Long topicId, String memberId, TopicCreate request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        // 발제 조회 및 존재 여부 확인
        checkmo.clubMeeting.internal.entity.Topic topic = clubTopicQueryService.validateTopic(topicId, meetingId);

        // 발제 작성자와 수정자가 같은지 확인
        if (!topic.isOwnedBy(clubMemberId)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        // 발제 수정
        topic.updateTopic(
                request.getDescription()
        );

        return topic.getId();
    }

    @Override
    public void deleteTopic(Long meetingId, Long topicId, String memberId) {
        // 1. 유효성 검증 (meeting clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        // 발제 조회 및 존재 여부 확인
        checkmo.clubMeeting.internal.entity.Topic topic = clubTopicQueryService.validateTopic(topicId, meetingId);

        // 발제 작성자와 삭제자가 같은지 확인
        if (!topic.isOwnedBy(clubMemberId)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        // 발제 삭제(Meeting의 orphanRemoval로 처리)
        topic.removeMeeting();
    }

    @Override
    public MeetingResponseDTO.TopicSelection selectOrCancelTopic(Long meetingId, Long topicId, String memberId,
                                                                 MeetingRequestDTO.TopicSelection request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        Long clubMemberId = clubManagementAPI.getActiveClubMemberInfo(meeting.getClubId(), memberId);

        // 팀, 발제 존재 여부 및 일치 여부 확인
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, request.getTeamNumber());
        checkmo.clubMeeting.internal.entity.Topic topic = clubTopicQueryService.validateTopic(topicId, meetingId);

        // 팀 발제가 존재하는지(선택된 상태인지) 확인
        Optional<TeamTopic> existingTeamTopic = teamTopicRepository.findByTeamIdAndTopicId(team.getId(), topic.getId());
        boolean isSelected = existingTeamTopic.isPresent();

        // 요청과 상태가 같으면 무시
        if (request.getIsSelected() == isSelected) {
            return ClubMeetingConverter.fromParametersToTopicSelectionDTO(topicId, request.getTeamNumber(), isSelected);
        }

        // 상태 변경
        if (request.getIsSelected()) {
            // 팀 발제 선택
            TeamTopic teamTopic = TeamTopic.builder()
                    .team(team)
                    .topic(topic)
                    .build();
            teamTopic.setTeam(team);
            teamTopic.setTopic(topic);

            try {
                teamTopicRepository.saveAndFlush(teamTopic);
            } catch (DataIntegrityViolationException e) {
                // 다른 쓰레드가 먼저 팀 발제를 선택한 경우, 선택 성공으로 간주
                teamTopic.removeTeam();
                teamTopic.removeTopic();
                return ClubMeetingConverter.fromParametersToTopicSelectionDTO(topicId, request.getTeamNumber(), true);
            }
            return ClubMeetingConverter.fromParametersToTopicSelectionDTO(topicId, request.getTeamNumber(), true);
        } else {
            // 팀 발제 선택 취소
            try {
                TeamTopic teamTopic = existingTeamTopic.get();
                // 연관관계 해제 및 orphanRemoval로 삭제 처리
                teamTopic.removeTeam();
                teamTopic.removeTopic();
                teamTopicRepository.flush();
                return ClubMeetingConverter.fromParametersToTopicSelectionDTO(topicId, request.getTeamNumber(), false);
            } catch (OptimisticLockingFailureException e) {
                // 다른 트랜잭션이 이미 삭제했거나 수정한 경우, 선택 해제 성공으로 간주
                return ClubMeetingConverter.fromParametersToTopicSelectionDTO(topicId, request.getTeamNumber(), false);
            }
        }
    }

}
