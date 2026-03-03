package checkmo.clubMeeting;

import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;

public interface ClubMeetingAPI {

    /**
     * 특정 모임의 상세 정보를 조회합니다.
     *
     * @param meetingId 조회할 모임 ID
     * @return 조회된 모임 상세 정보 DTO
     */
    DetailInfo fetchMeetingDetailInfo(Long meetingId);

    /**
     * 팀이 특정 동아리에 속하지 않는지 확인합니다.
     *
     * @param clubId 조회할 동아리 ID
     * @param teamId 조회할 팀 ID
     */
    boolean isNotTeamBelongsToClub(Long clubId, Long teamId);

    /**
     * 미팅이 특정 동아리에 속하지 않는지 확인합니다.
     *
     * @param clubId    동아리 ID
     * @param meetingId 모임 ID
     */
    boolean isNotMeetingBelongsToClub(Long clubId, Long meetingId);

    /**
     * 특정 정기모임의 팀원이 맞는지 확인합니다.
     *
     * @param teamId       팀 ID
     * @param clubMemberId 멤버 ID
     */
    boolean isTeamMember(Long teamId, Long clubMemberId);

    /**
     * 팀의 발제를 선택 또는 해제합니다.
     *
     * @param clubId    동아리 ID
     * @param meetingId
     * @param teamId    팀 ID
     * @param topicId   발제 ID
     * @param selected  선택 여부 (true: 선택, false: 해제)
     * @return true: 발제 선택, false: 발제 해제
     */
    boolean toggleTopic(Long clubId, Long meetingId, Long teamId, Long topicId, boolean selected, String memberId);
}
