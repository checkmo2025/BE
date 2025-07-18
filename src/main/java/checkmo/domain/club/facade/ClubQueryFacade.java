package checkmo.domain.club.facade;

import checkmo.domain.club.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.global.dto.ClubSharedDTO;

/**
 * Club Domain Query Facade
 *
 * Club 도메인의 모든 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface ClubQueryFacade {

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (외부용)
     * 마이페이지 등 다른 서비스에서 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 회원이 가입한 모임의 간략한 정보 목록 DTO
     */
    ClubSharedDTO.MyClubListDTO getMyClubListForShare(String memberId);

    /**
     * 특정 회원이 가입한 모임 목록을 size 개수만큼 조회합니다. (외부용)
     *
     * @param memberId 회원 ID
     * @param size     조회할 개수
     * @return 회원이 가입한 모임의 간략한 정보 목록 DTO
     */
    ClubSharedDTO.MyClubListDTO getMyClubListForShare(String memberId, int size);

    /**
     * ClubQueryService
     * 조건에 맞는 독서 모임 목록을 검색합니다. (내부용)
     *
     * @param keyword      검색 키워드
     * @param region       지역 필터링 여부
     * @param participants 대상 필터링 여부
     * @param cursorId     페이징 커서 ID
     * @return 검색된 모임 목록 DTO
     */
    ClubResponseDTO.ClubListDTO getClubList(String keyword, int region, int participants, Long cursorId);

    /**
     * ClubQueryService
     * 독서 모임의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId   조회할 모임 ID
     * @param memberId 조회자 회원 ID
     * @return 모임 상세 정보 DTO
     */
    ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId);

    /**
     * ClubQueryService
     * 특정 상태의 모임 회원 목록을 조회합니다. (내부용)
     *
     * @param clubId           모임 ID
     * @param memberId         요청자(운영진) 회원 ID
     * @param clubMemberStatus 조회할 회원 상태
     * @param cursorId         페이징 커서 ID
     * @return 해당 상태의 회원 목록 DTO
     */
    ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId);

    /**
     * ClubQueryService
     * 모임 이름의 중복 여부를 확인합니다. (내부용)
     *
     * @param clubName 확인할 모임 이름
     * @return 중복 시 true
     */
    boolean isDuplicateClubName(String clubName);

    /**
     * ClubQueryService
     * 모임의 전체 공지사항 목록을 최신순으로 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 조회자 회원 ID
     * @param cursorId 페이징 커서 ID
     * @return 전체 공지사항 목록 DTO
     */
    ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId);

    /**
     * ClubQueryService
     * 모임의 중요 공지사항을 최신순으로 size 개수만큼 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 조회자 회원 ID
     * @param size     조회할 개수
     * @return 중요 공지사항 목록 DTO
     */
    ClubResponseDTO.ClubNoticeListDTO getImportantNotices(Long clubId, String memberId, int size);

    /**
     * ClubQueryService
     * 공지사항(투표 포함)의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param noticeId 조회할 공지사항 ID
     * @return 공지사항 상세 정보 DTO
     */
    ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId);

    /**
     * ClubBookRecommendQueryService
     * 모임의 추천 책 목록을 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param cursorId 페이징 커서 ID
     * @return 추천 책 목록 DTO
     */
    ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId);

    /**
     * ClubBookRecommendQueryService
     * 추천 책의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId          모임 ID
     * @param bookRecommendId 추천 책 ID
     * @return 추천 책 상세 정보 DTO
     */
    ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(Long clubId, Long bookRecommendId);

    /**
     * ClubBookShelfQueryService
     * 모임의 책장 목록을 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param cursorId 페이징 커서 ID
     * @return 책장 목록 DTO
     */
    BookShelfResponseDTO.BookShelfListDTO getBookShelfList(Long clubId, Long cursorId);

    /**
     * ClubBookShelfQueryService
     * 책장의 상세 정보를 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @return 책장 상세 정보 DTO
     */
    BookShelfResponseDTO.BookShelfDetailDTO getBookShelfDetail(Long meetingId);

    /**
     * ClubMeetingQueryService
     * 모임의 모든 미팅 목록을 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param cursorId 페이징 커서 ID
     * @return 미팅 목록 DTO
     */
    MeetingResponseDTO.MeetingListDTO findAllMeetingsByClub(Long clubId, Long cursorId);

    /**
     * ClubMeetingQueryService
     * 특정 미팅의 상세 정보를 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @return 미팅 상세 정보 DTO
     */
    MeetingResponseDTO.InProgressMeetingDetailDTO findMeetingById(Long meetingId);

    /**
     * ClubMeetingQueryService
     * 특정 미팅의 전체 발제(토픽) 목록을 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @param cursorId  페이징 커서 ID
     * @return 발제 목록 DTO
     */
    MeetingResponseDTO.TopicListDTO findTopicsByMeeting(Long meetingId, Long cursorId);

    /**
     * ClubMeetingQueryService
     * 특정 미팅의 팀별 정보를 조회합니다. (내부용)
     *
     * @param meetingId  미팅 ID
     * @param teamNumber 조회할 팀 번호
     * @return 해당 팀의 정보 DTO
     */
    MeetingResponseDTO.TeamDTO findTeamDetailsByMeeting(Long meetingId, Integer teamNumber);
}