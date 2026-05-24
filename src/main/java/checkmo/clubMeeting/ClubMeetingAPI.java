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
     * 미팅이 특정 동아리에 속하지 않는지 확인합니다.
     *
     * @param clubId    동아리 ID
     * @param meetingId 모임 ID
     */
    boolean isNotMeetingBelongsToClub(Long clubId, Long meetingId);

    /**
     * 팀이 특정 동아리에 속하지 않는지 확인합니다.
     *
     * @param clubId 동아리 ID
     * @param teamId 팀 ID
     */
    boolean isNotTeamBelongsToClub(Long clubId, Long teamId);

    /**
     * 사용자가 팀의 구성원이 아닌지 확인합니다.
     *
     * @param teamId       팀 ID
     * @param clubMemberId 동아리 회원 ID
     */
    boolean isNotTeamMember(Long teamId, Long clubMemberId);

    /**
     * 팀의 발제를 선택 또는 해제합니다.
     *
     * @param meetingId 모임 ID
     * @param teamId    팀 ID
     * @param topicId   발제 ID
     * @param selected  선택 여부 (true: 선택, false: 해제)
     * @return true: 발제 선택, false: 발제 해제
     */
    ClubMeetingExternalDTO.ToggleTopicResult toggleTopic(Long meetingId, Long teamId, Long topicId, boolean selected);

    /**
     * 특정 모임에서 채팅이 불가능한지 확인합니다.
     *
     * @param meetingId 모임 ID
     * @return true: 채팅 불가능, false: 채팅 가능
     */
    boolean isChatDisabled(Long meetingId);

    ClubMeetingExternalDTO.TopicReportInfo fetchTopicReportInfo(Long topicId);

    ClubMeetingExternalDTO.BookReviewReportInfo fetchBookReviewReportInfo(Long bookReviewId);
}
