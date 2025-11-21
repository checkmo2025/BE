package checkmo.clubMeeting.internal.service.command;

import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;

/**
 * 독서모임의 '미팅'과 '팀'에 대한 비즈니스 요구사항을 수행합니다.
 */
public interface ClubMeetingCommandService {

    /**
     * 독서모임의 미팅을 생성합니다.
     *
     * @param clubId   미팅을 생성할 독서모임 ID
     * @param memberId 생성자(운영진) 회원 ID
     * @param request  미팅 생성 요청 DTO
     * @return 생성한 미팅 ID
     */
    Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreate request);

    /**
     * 독서모임의 미팅을 수정합니다.
     *
     * @param meetingId 수정할 미팅 ID
     * @param memberId  요청자(운영진) 회원 ID
     * @param request   미팅 수정 요청 DTO
     * @return 수정한 미팅 ID
     */
    Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdate request);

    /**
     * 독서모임의 팀을 구성합니다.
     *
     * @param meetingId 미팅 ID 팀 구성 요청자
     * @param memberId  요청자(운영진) 회원 ID
     * @param request   팀 구성 요청 DTO
     */
    void manageTeam(Long meetingId, String memberId, MeetingRequestDTO.TeamManage request);

}
