package checkmo.domain.club.service.command;

import checkmo.domain.club.dto.meeting.MeetingRequestDTO;

/**
 * 독서모임의 미팅 생성, 발제 작성, 팀 구성, 독서 후기 등등
 * 독서모임의 토론 전체를 관리
 */
public interface ClubMeetingCommandService {

    /**
     * 독서모임의 미팅을 생성합니다.
     *
     * @param clubId 독서모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인
     * @param request 미팅 생성 요청 DTO
     * @result 생성한 미팅 ID
     */
     Long createMeeting(Long clubId, Long memberId, MeetingRequestDTO.MeetingCreateRequestDTO request);

    /**
     * 독서모임의 발제를 작성합니다.
     *
     * @param memberId 발제 작성자의 ID
     * @param meetingId 미팅 ID -> 미팅 ID만 알아도 어느 Club인지 알 수 있기 때문에 ClubId는 필요 없음
     * @param request 발제 내용 DTO
     * @return 생성한 발제 ID
     */
    Long createPresentation(Long memberId, Long meetingId, MeetingRequestDTO.TopicDTO request);
}
