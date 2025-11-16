package checkmo.clubMeeting.internal.service.command;

import checkmo.clubMeeting.web.dto.meeting.MeetingRequestDTO;

/**
 * 독서모임의 '미팅'과 '팀'에 대한 비즈니스 요구사항을 수행합니다.
 */
public interface ClubMeetingCommandService {

    /**
     * 독서모임의 미팅을 생성합니다.
     * <p>
     * 피그마 참고 페이지 : #독서모임 - 운영진 화면 [모임 생성하기] 클릭시 - 두번째 화면 - 가장 최신 걸로 (와이어프레임 제일 아래)
     *
     * @param clubId   미팅을 생성할 독서모임 ID
     * @param memberId 생성자(운영진) 회원 ID
     * @param request  미팅 생성 요청 DTO
     * @return 생성한 미팅 ID
     * <p>
     * ‼️ 내부에서 공지사항 자동으로 연결해서 생성해주는 로직 반드시 필요
     */
    Long createMeeting(Long clubId, String memberId, MeetingRequestDTO.MeetingCreate request);

    /**
     * 독서모임의 미팅을 수정합니다.
     * <p>
     * 피그마 참고 페이지 : 피그마 페이지 없음
     *
     * @param meetingId 수정할 미팅 ID
     * @param memberId  요청자(운영진) 회원 ID
     * @param request   미팅 수정 요청 DTO
     * @return 수정한 미팅 ID
     * <p>
     * ‼️ 내부에서 공지사항 자동으로 연결해서 기존 공지사항 삭제하고 재생성해주는 로직 반드시 필요
     */
    Long updateMeeting(Long meetingId, String memberId, MeetingRequestDTO.MeetingUpdate request);

    /**
     * 독서모임의 팀을 구성합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임 - 운영진 화면 [조 관리하기] 클릭시
     *
     * @param meetingId 미팅 ID 팀 구성 요청자
     * @param memberId  요청자(운영진) 회원 ID
     * @param request   팀 구성 요청 DTO
     */
    void manageTeam(Long meetingId, String memberId, MeetingRequestDTO.TeamManage request);

}
