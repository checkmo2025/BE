package checkmo.domain.club.service.query;

import checkmo.domain.club.dto.meeting.MeetingResponseDTO;

/**
 * 독서 모임의 토론 조회 서비스
 *
 * 독서 모임 토론에 대한 조회 기능을 담당
 * ex) 토론 일정 조회, 토론 참석자 목록 조회, 한줄평(책장) 등등 조회 등을 처리
 */
public interface ClubMeetingQueryService {

    /**
     * 독서 모임의 미팅 참석자 목록을 조회합니다.
     *
     * @param memberId 조회하는 사람 ID -> 운영진만 조회 가능
     * @param meetingId 미팅 ID
     * @return 조회한 미팅 정보 DTO
     */
    MeetingResponseDTO.ParticipantDTO findMeetingAttendees(Long memberId, Long meetingId);

    /**
     * 독서 모임의 특정 미팅을 조회합니다.
     *
     * @param meetingId 미팅 ID
     * @return 조회한 미팅 정보 DTO -> 발제는 4개, 각 조당 인원은 4개씩만 담기
     */
    MeetingResponseDTO.InProgressMeetingDetailDTO findMeetingById(Long meetingId);

    /**
     * 독서 모임의 모든 미팅을 조회합니다.
     *
     * @param clubId 독서 모임 ID
     * @return 모든 미팅 정보 DTO 리스트
     */
    MeetingResponseDTO.MeetingListDTO findAllMeetingsByClub(Long clubId);

    /**
     * 독서 모임의 미팅 토픽을 조회합니다.
     *
     * @param meetingId 미팅 ID
     * @return 조회한 토픽 정보 DTO
     */
    MeetingResponseDTO.TopicListDTO findTopicsByMeeting(Long meetingId);

    /**
     * 독서 모임의 미팅의 팀별 인원 조회 (해당 팀이 선택한 발제 리스트도 포함)
     *
     * @param meetingId 미팅 ID
     * @param teamNumber 팀 번호 (1, 2, 3, 4 팀 -> 실제로 프론트에서는 A, B, C, D로 표시)
     * @return 조회한 팀 정보 DTO
     */
    MeetingResponseDTO.TeamDTO findTeamsByMeeting(Long meetingId, Integer teamNumber);
}
