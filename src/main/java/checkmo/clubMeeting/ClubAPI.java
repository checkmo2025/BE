package checkmo.clubMeeting;

import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.clubMeeting.web.dto.meeting.MeetingResponseDTO;

/**
 * Club Domain Query Facade
 * <p>
 * Club 도메인의 모든 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface ClubAPI {

    /**
     * ClubMeetingQueryService [책장] 모임의 책장 목록을 조회합니다. (내부용)
     *
     * @param clubId     모임 ID
     * @param cursorId   페이징 커서 ID
     * @param size       조회할 개수
     * @param generation 미팅 기수 (1기, 2기 등)
     * @param memberId   요청자 회원 ID
     * @return 책장 목록 DTO
     */
    BookShelfResponseDTO.BookShelfListDTO getBookShelfList(Long clubId, Long cursorId, Integer size, Integer generation,
                                                           String memberId);

    /**
     * ClubMeetingQueryService [책장] 책장의 상세 정보를 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @return 책장 상세 정보 DTO
     */
    BookShelfResponseDTO.BookShelfDetailDTO getBookShelfDetail(Long meetingId, String memberId);

    /**
     * ClubMeetingQueryService [책장] 특정 미팅의 전체 발제(토픽) 목록을 조회합니다.(팀 정보 X) (내부용)
     *
     * @param meetingId 미팅 ID
     * @param cursorId  페이징 커서 ID
     * @param size      조회할 개수
     * @param memberId  요청자 회원 ID
     * @return 발제 목록 DTO
     */
    BookShelfResponseDTO.TopicListDTO findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId);

    /**
     * ClubBookShelfQueryService [책장] 특정 미팅에 대한 한줄평을 size만큼 조회합니다. (내부용)
     *
     * @param meetingId    미팅 ID
     * @param lastReviewId 마지막으로 조회한 한줄평 ID (무한 스크롤용)
     * @param size         조회할 개수
     * @param memberId     요청자 회원 ID
     * @return 한줄평 목록 DTO
     */
    BookShelfResponseDTO.BookReviewListDTO getBookReviewList(Long meetingId, Long lastReviewId, int size,
                                                             String memberId);

    /**
     * ClubMeetingQueryService [모임] 모임의 모든 미팅 목록을 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param cursorId 페이징 커서 ID
     * @param size     조회할 개수
     * @param memberId 요청자 회원 ID
     * @return 미팅 목록 DTO
     */
    MeetingResponseDTO.MeetingListDTO getMeetingsByClub(Long clubId, Long cursorId, Integer size, String memberId);

    /**
     * ClubMeetingQueryService [모임] 특정 미팅의 상세 정보를 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @param memberId  요청자 회원 ID
     * @return 미팅 상세 정보 DTO
     */
    MeetingResponseDTO.MeetingDetailDTO findMeetingDetailById(Long meetingId, String memberId);

    /**
     * ClubMeetingQueryService [모임] 특정 미팅의 발제 목록을 조회합니다.(선택한 팀 정보도 함꼐) (내부용)
     *
     * @param meetingId 미팅 ID
     * @param memberId  요청자 회원 ID
     * @return 미팅 발제 목록 DTO
     */
    MeetingResponseDTO.TopicDTOList findMeetingTopicsWithTeam(Long meetingId, String memberId);

    /**
     * ClubMeetingQueryService [모임] 특정 미팅의 팀별 발제를 조회합니다. (내부용)
     *
     * @param meetingId  미팅 ID
     * @param teamNumber 조회할 팀 번호
     * @param memberId   요청자 회원 ID
     * @return 해당 팀의 정보 DTO
     */
    MeetingResponseDTO.TeamTopicDTO findMeetingTopicsByTeam(Long meetingId, Integer teamNumber, String memberId);

    /**
     * ClubMeetingQueryService
     * <p>
     * [모임] 특정 미팅의 참여 인원 목록을 조회합니다.(내부용)
     *
     * @param meetingId 미팅 ID
     * @param memberId  요청자 회원 ID
     */
    MeetingResponseDTO.MeetingMemberListDTO findMeetingMembersByMeeting(Long meetingId, Long cursorId, Integer size,
                                                                        String memberId);

    /**
     * ClubMeetingQueryService
     * <p>
     * [모임] 특정 미팅의 팀별 참여 인원 목록을 조회합니다. (내부용)
     *
     * @param meetingId  미팅 ID
     * @param teamNumber 조회할 팀 번호
     * @param memberId   요청자 회원 ID
     * @return 해당 팀의 참여 인원 목록 DTO
     */
    MeetingResponseDTO.TeamMemberDTO findTeamMembersByMeeting(Long meetingId, Integer teamNumber, String memberId);

    /**
     * ClubMeetingQueryService 특정 클럽의 모임 캘린더를 조회합니다. (내부용)
     *
     * @param clubId   클럽 ID
     * @param year     조회할 연도
     * @param month    조회할 월
     * @param memberId 요청자 회원 ID
     * @return 미팅 리스트 DTO
     */
    MeetingResponseDTO.CalendarMeetingDTO getClubMeetingCalendar(Long clubId, int year, int month, String memberId);

}