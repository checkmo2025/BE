package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.ClubMemberTeamRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

    public List<Integer> retrieveExistingTeamNumbers(Long meetingId) {
        return teamRepository.findTeamNumberByMeetingId(meetingId);
    }

    public Map<Long, Long> retrieveTeamIdByClubMemberId(List<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return Map.of();
        }
        List<ClubMemberTeam> clubMemberTeams = clubMemberTeamRepository.findAllByTeamIds(teamIds);
        return clubMemberTeams.stream()
                .collect(Collectors.toMap(
                        ClubMemberTeam::getClubMemberId, // key: 클럽멤버 ID
                        clubMemberTeam -> clubMemberTeam.getTeam().getId() // value: 팀 id
                        // 하나의 멤버는 하나의 미팅의 여러 팀에 속할 수 없으므로 병합 조건 존재하지 않아도 됨
                ));
    }

    public Set<Long> retrieveSelectedTopicIds(Long teamId, List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(teamTopicRepository.findTopicIdsByTeamIdAndTopicIds(teamId, topicIds));
    }

    public boolean isBelongsToClub(Long clubId, Long teamId) {
        Long actualClubId = teamRepository.findClubIdByTeamId(teamId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.TEAM_NOT_FOUND));
        return actualClubId.equals(clubId);
    }

    public boolean isTeamMember(Long teamId, Long clubMemberId) {
        return clubMemberTeamRepository.existsByTeamIdAndClubMemberId(teamId, clubMemberId);
    }

    public Team validateTeam(Long meetingId, Long teamId) throws ClubMeetingException {
        return teamRepository.findByMeetingIdAndId(meetingId, teamId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.TEAM_NOT_FOUND));
    }

    public Team validateTeam(Long meetingId, Integer teamNumber) throws ClubMeetingException {
        return teamRepository.findByMeetingIdAndTeamNumber(meetingId, teamNumber)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.TEAM_NOT_FOUND));
    }
}
