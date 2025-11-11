package checkmo.clubMeeting.internal.service.command;

import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.service.query.ClubMemberQueryService;
import checkmo.clubManagement.internal.service.query.ClubQueryService;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.TopicDTO;
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

    // 외부의 QueryService
    private final ClubQueryService clubQueryService;
    private final ClubMemberQueryService clubMemberQueryService;

    // 자신의 QueryService
    private final ClubMeetingQueryService clubMeetingQueryService;

    // 자신의 Repository
    private final TopicRepository topicRepository;
    private final TeamTopicRepository teamTopicRepository;

    @Override
    public Long createTopic(Long meetingId, String memberId, TopicDTO request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 생성자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 3. 발제 생성
        Topic topic = ClubMeetingConverter.fromTopicDTOToTopic(request);
        topic.setMeeting(meeting);
        topic.setClubMember(clubMember);

        // 4. 발제 저장
        return topicRepository.save(topic).getId();
    }

    @Override
    public Long updateTopic(Long meetingId, Long topicId, String memberId, TopicDTO request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 수정자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 3. 발제 조회 및 존재 여부 확인
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);

        // 4. 발제 작성자와 수정자가 같은지 확인
        if (!topic.isOwnedBy(clubMember)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        // 5. 발제 수정
        topic.updateTopic(
                request.getDescription()
        );

        return topic.getId();
    }

    @Override
    public void deleteTopic(Long meetingId, Long topicId, String memberId) {
        // 1. 유효성 검증 (meeting clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 삭제자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 3. 발제 조회 및 존재 여부 확인
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);

        // 4. 발제 작성자와 삭제자가 같은지 확인
        if (!topic.isOwnedBy(clubMember)) {
            throw new GeneralException(ErrorStatus.TOPIC_FORBIDDEN);
        }

        // 5. 발제 삭제
        topic.removeMeeting();
        topic.removeClubMember();
    }

    @Override
    public MeetingResponseDTO.TopicSelectionDTO selectOrCancelTopic(Long meetingId, Long topicId, String memberId,
                                                                    MeetingRequestDTO.TopicSelectionDTO request) {
        // 1. 유효성 검증 (meeting, clubMember)
        Meeting meeting = clubMeetingQueryService.validateMeeting(meetingId);
        ClubMember clubMember = clubMemberQueryService.validateClubMember(meeting.getClubId(), memberId);

        // 2. 발제 선택자 활성화 여부 확인
        if (!clubMember.isActive()) {
            throw new GeneralException(ErrorStatus.CLUB_MEMBER_IS_NOT_ACTIVE);
        }

        // 3. 팀, 발제 존재 여부 및 일치 여부 확인
        Team team = clubMeetingQueryService.validateTeam(meetingId, request.getTeamNumber());
        Topic topic = clubMeetingQueryService.validateTopic(topicId, meetingId);

        // 4. 팀 발제가 존재하는지(선택된 상태인지) 확인
        Optional<TeamTopic> existingTeamTopic = teamTopicRepository.findByTeamIdAndTopicId(team.getId(), topic.getId());
        boolean isSelected = existingTeamTopic.isPresent();

        // 5. 요청과 상태가 같으면 무시
        if (request.getIsSelected() == isSelected) {
            return ClubMeetingConverter.fromParametersToTopicSelectionDTO(topicId, request.getTeamNumber(), isSelected);
        }

        // 6. 상태 변경
        if (request.getIsSelected()) {
            // 6-1. 팀 발제 선택
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
            // 6-2. 팀 발제 선택 취소
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
