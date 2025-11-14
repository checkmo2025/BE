package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.MemberTeam;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.clubMeeting.internal.repository.MemberTeamRepository;
import checkmo.clubMeeting.internal.repository.TeamRepository;
import checkmo.clubMeeting.internal.repository.TeamTopicRepository;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
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
public class ClubMeetingTeamQueryServiceImpl implements ClubMeetingTeamQueryService {

    private final TeamRepository teamRepository;
    private final TeamTopicRepository teamTopicRepository;
    private final MemberTeamRepository memberTeamRepository;

    @Override
    public List<Team> findTeamsByMeeting(Long meetingId) {
        return teamRepository.findAllByMeetingIdOrderByTeamNumberAsc(meetingId);
    }

    @Override
    public List<MemberTeam> getMemberTeamsByTeam(Long teamId) {
        return memberTeamRepository.findAllWithClubMemberByTeamIds(List.of(teamId));
    }

    @Override
    public Map<String, Long> getMemberIdToTeamIdMap(List<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return Map.of();
        }
        List<MemberTeam> memberTeams = memberTeamRepository.findAllWithClubMemberByTeamIds(teamIds);
        return memberTeams.stream()
                .collect(Collectors.toMap(
                        mt -> mt.getClubMember().getMemberId(), // key: 멤버 ID
                        MemberTeam::getTeamId // value: 팀 id
                        // 하나의 멤버는 하나의 미팅의 여러 팀에 속할 수 없으므로 병합 조건 존재하지 않아도 됨
                ));
    }

    @Override
    public List<TeamTopic> findTeamTopicsWithTopicAndClubMemberByTeamId(Long teamId, Integer size) {
        Pageable pageable = (size == null) ? Pageable.unpaged() : PageRequest.of(0, size);
        return teamTopicRepository.findAllWithTopicAndClubMemberByTeamIdOrderByDesc(teamId, pageable);
    }

    @Override
    public Map<Long, List<Integer>> findTeamTopicsWithTeamByTopicIds(List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return Map.of();
        }

        List<TeamTopic> teamTopics = teamTopicRepository.findAllWithTeamByTopicIds(topicIds);
        return teamTopics.stream()
                .collect(Collectors.groupingBy(
                        TeamTopic::getTopicId, //key: 토픽 ID(토픽 ID로 그룹화)
                        Collectors.mapping(tt -> tt.getTeam().getTeamNumber(), Collectors.toList())
                        //value: 해당 토픽을 선택한 팀 번호 리스트(같은 그룹에 속하는 TeamTopic의 팀 번호 List 생성)
                ));
    }

    @Override
    public Team validateTeam(Long meetingId, Integer teamNumber) throws GeneralException {
        return teamRepository.findByMeetingIdAndTeamNumber(meetingId, teamNumber)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TEAM_NOT_FOUND));
    }
}
