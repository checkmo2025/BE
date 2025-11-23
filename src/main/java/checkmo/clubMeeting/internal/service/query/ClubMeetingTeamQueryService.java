package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.ClubMemberTeamRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubMeetingTeamQueryService {

    private final TeamRepository teamRepository;
    private final TeamTopicRepository teamTopicRepository;
    private final ClubMemberTeamRepository clubMemberTeamRepository;

    public List<Team> retrieveTeams(Long meetingId) {
        return teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meetingId);
    }
    
    public List<ClubMemberTeam> retrieveClubMemberTeams(Long teamId) {
        return clubMemberTeamRepository.findAllByTeamIds(List.of(teamId));
    }

    public Map<Long, Long> retrieveTeamIdByClubMemberId(List<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return Map.of();
        }
        List<ClubMemberTeam> clubMemberTeams = clubMemberTeamRepository.findAllByTeamIds(teamIds);
        return clubMemberTeams.stream()
                .collect(Collectors.toMap(
                        ClubMemberTeam::getClubMemberId, // key: 클럽멤버 ID
                        ClubMemberTeam::getTeamId // value: 팀 id
                        // 하나의 멤버는 하나의 미팅의 여러 팀에 속할 수 없으므로 병합 조건 존재하지 않아도 됨
                ));
    }

    public List<TeamTopic> retrieveTeamTopics(Long teamId, Integer size) {
        Pageable pageable = (size == null) ? Pageable.unpaged() : PageRequest.of(0, size);
        return teamTopicRepository.findAllWithTopicByTeamIdOrderByDesc(teamId, pageable);
    }

    public Map<Long, List<Integer>> retrieveSelectedTeamNumbersByTopicIds(List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return Map.of();
        }

        List<TeamTopic> teamTopics = teamTopicRepository.findAllWithTeamByTopicIds(topicIds);
        return teamTopics.stream()
                .collect(Collectors.groupingBy(
                        TeamTopic::getTopicId, //key: 토픽 ID
                        Collectors.mapping(tt -> tt.getTeam().getTeamNumber(), Collectors.toList())
                        //value: 해당 토픽을 선택한 팀 번호 리스트(같은 그룹에 속하는 TeamTopic의 팀 번호 List 생성)
                ));
    }

    public Team validateTeam(Long meetingId, Integer teamNumber) throws ClubMeetingException {
        return teamRepository.findByMeetingIdAndTeamNumber(meetingId, teamNumber)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.TEAM_NOT_FOUND));
    }
}
