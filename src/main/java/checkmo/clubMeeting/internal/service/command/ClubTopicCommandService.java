package checkmo.clubMeeting.internal.service.command;

import checkmo.clubManagement.ClubManagementAPI;
import checkmo.clubManagement.ClubManagementExternalDTO;
import checkmo.clubMeeting.internal.converter.ClubMeetingConverter;
import checkmo.clubMeeting.internal.entity.ClubMeetingActor;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import checkmo.clubMeeting.internal.repository.TopicRepository;
import checkmo.clubMeeting.internal.service.query.ClubMeetingQueryService;
import checkmo.clubMeeting.internal.service.query.ClubMeetingTeamQueryService;
import checkmo.clubMeeting.internal.service.query.ClubTopicQueryService;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO.TopicCreate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubTopicCommandService {

    private final ClubManagementAPI clubManagementAPI;

    private final ClubMeetingQueryService clubMeetingQueryService;
    private final ClubTopicQueryService clubTopicQueryService;
    private final ClubMeetingTeamQueryService clubMeetingTeamQueryService;

    private final TopicRepository topicRepository;
    private final TeamTopicRepository teamTopicRepository;

    public void createTopic(Long clubId, Long meetingId, Long memberId, TopicCreate request) {
        clubManagementAPI.validateClub(clubId);
        Long clubMemberId = clubManagementAPI.validateAndFetchActiveClubMemberId(clubId, memberId);
        Meeting meeting = clubMeetingQueryService.validateMeeting(clubId, meetingId);

        Topic topic = ClubMeetingConverter.toTopic(request, memberId, clubMemberId);
        topic.setMeeting(meeting);

        topicRepository.save(topic);
    }

    public void updateTopic(Long clubId, Long meetingId, Long topicId, Long memberId, TopicCreate request) {
        clubManagementAPI.validateClub(clubId);
        ClubManagementExternalDTO.MembershipInfo clubMembership = clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        if (!clubMembership.isActive()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.CLUB_MEMBER_INACTIVE);
        }
        ClubMeetingActor actor = new ClubMeetingActor(
                clubMembership.getClubMemberId(),
                clubMembership.isStaff()
        );
        clubMeetingQueryService.validateMeeting(clubId, meetingId);

        Topic topic = clubTopicQueryService.validateTopic(topicId, meetingId);
        topic.updateBy(actor, request.getDescription());
    }

    public void deleteTopic(Long clubId, Long meetingId, Long topicId, Long memberId) {
        clubManagementAPI.validateClub(clubId);
        ClubManagementExternalDTO.MembershipInfo clubMembership = clubManagementAPI.fetchMembershipInfo(clubId, memberId);
        if (!clubMembership.isActive()) {
            throw new ClubMeetingException(ClubMeetingErrorStatus.CLUB_MEMBER_INACTIVE);
        }
        ClubMeetingActor actor = new ClubMeetingActor(
                clubMembership.getClubMemberId(),
                clubMembership.isStaff()
        );
        clubMeetingQueryService.validateMeeting(clubId, meetingId);

        Topic topic = clubTopicQueryService.validateTopic(topicId, meetingId);
        topic.removeBy(actor);
    }

    public boolean toggleTopic(
            Long meetingId,
            Long teamId,
            Long topicId,
            boolean selected
    ) {
        Team team = clubMeetingTeamQueryService.validateTeam(meetingId, teamId);
        Topic topic = clubTopicQueryService.validateTopic(topicId, meetingId);

        // 팀 발제가 존재하는지(선택된 상태인지) 확인
        Optional<TeamTopic> existingTeamTopic = teamTopicRepository.findByTeamIdAndTopicId(team.getId(), topic.getId());
        boolean currentlySelected = existingTeamTopic.isPresent();

        // 요청과 상태가 같으면 무시
        if (selected == currentlySelected) {
            return selected;
        }

        // 상태 변경
        if (selected) {
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

                // 예외가 중복키 경쟁이었는지 확인
                boolean exists = teamTopicRepository.existsByTeamIdAndTopicId(team.getId(), topic.getId());
                if (exists) {
                    return true;
                }
                throw e;
            }
            return true;
        }
        // 팀 발제 선택 취소
        try {
            TeamTopic teamTopic = existingTeamTopic.get();
            teamTopic.removeTeam();
            teamTopic.removeTopic();
            teamTopicRepository.flush();
        } catch (OptimisticLockingFailureException e) {
            // 다른 트랜잭션이 이미 삭제했거나 수정한 경우, 선택 해제 성공으로 간주
        }
        return false;
    }
}
