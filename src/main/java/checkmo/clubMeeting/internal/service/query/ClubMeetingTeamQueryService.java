package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.MemberTeam;
import checkmo.clubMeeting.internal.entity.Team;
import checkmo.clubMeeting.internal.entity.TeamTopic;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import java.util.Map;

/**
 * 독서 모임의 팀 관련 조회 서비스
 */
public interface ClubMeetingTeamQueryService {

    /**
     * 독서 모임 특정 미팅에 존재하는 모든 팀 정보를 조회합니다.
     *
     * @return 조회한 팀 리스트
     * @Param meetingId 미팅 ID
     */
    List<Team> findTeamsByMeeting(Long meetingId);

    /**
     * 독서모임의 팀 멤버 정보를 조회합니다.
     *
     * @param teamId 팀 ID
     * @return MemberTeam 리스트
     */
    List<MemberTeam> getMemberTeamsByTeam(Long teamId);

    /**
     * 독서모임의 멤버 id에 따라 해당 클럽 멤버가 소속하는 팀 id를 매핑한 맵을 조회합니다.
     *
     * @param teamIds 팀 ID 목록
     */
    Map<String, Long> getMemberIdToTeamIdMap(List<Long> teamIds);

    /**
     * 독서 모임 미팅의 팀별 발제 조회
     *
     * @param teamId 미팅 ID
     * @param size   조회할 팀 토픽 개수 (null이면 전체 조회)
     * @return TeamTopic 리스트
     */
    List<TeamTopic> findTeamTopicsWithTopicAndClubMemberByTeamId(Long teamId, Integer size);

    /**
     * 특정 토픽 ID 목록에 해당하는 팀 토픽과 팀 정보를 조회한 후, 토픽 ID를 기준으로 해당 토픽을 선택한 팀 번호 리스트를 반환합니다.
     *
     * @param topicIds 조회할 토픽 ID 목록
     * @return 토픽 id를 기준으로 선택한 팀 번호 리스트 Map
     */
    Map<Long, List<Integer>> findTeamTopicsWithTeamByTopicIds(List<Long> topicIds);


    /**
     * 독서모임의 팀이 존재하는지 확인합니다.
     *
     * @param meetingId  미팅 ID
     * @param teamNumber 팀 번호 (1, 2, 3, 4... 팀)
     * @return Team 존재하는 팀 객체
     */
    Team validateTeam(Long meetingId, Integer teamNumber) throws GeneralException;

}
