package checkmo.domain.club.facade;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.global.dto.ClubSharedDTO;

import java.util.List;

/**
 * Club Domain Query Facade
 *
 * Club 도메인의 모든 Query(조회) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface ClubQueryFacade {

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (내부용)
     *
     * 피그마 참고 페이지 : #독서모임 - 내 모임 바로가기
     *
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    ClubResponseDTO.MyClubListDTO getMyClubList(String memberId);

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (내부용)
     *
     * 피그마 참고 페이지 : #마이페이지
     *
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    ClubResponseDTO.MyPageClubListDTO getMyPageClubList(String memberId);

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (외부용)
     * 마이페이지 등 다른 서비스에서 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 회원이 가입한 모임의 간략한 정보 목록 DTO
     */
    ClubSharedDTO.MyClubList getMyClubListForShare(String memberId);

    /**
     * ClubQueryService
     * 조건에 맞는 독서 모임 목록을 검색합니다. (내부용)
     *
     * @param memberId 요청자 회원 ID (해당 클럽 회원인지 확인용)
     * @param keyword 검색 키워드 (모임명 등)
     * @param region 지역 필터링 여부
     * @param participants 대상 필터링 여부
     * @param cursorId 페이징 커서 ID
     * @return 검색된 모임 목록 DTO
     */
    ClubResponseDTO.ClubListDTO getClubList(String memberId, String keyword, int region, int participants, Long cursorId, Integer size);

    /**
     * ClubQueryService
     * 독서 모임의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 조회할 모임 ID
     * @param memberId 조회자 회원 ID
     * @return 모임 상세 정보 DTO
     */
    ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId);

    /**
     * ClubQueryService
     * 특정 상태의 모임 회원 목록을 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param clubMemberStatus 조회할 회원 상태
     * @param cursorId 페이징 커서 ID
     * @return 해당 상태의 회원 목록 DTO
     */
    ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId, Integer size);

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
     * @param clubId 모임 ID
     * @param memberId 조회자 회원 ID
     * @param cursorId 페이징 커서 ID
     * @param onlyImportant 중요 공지사항만 조회할지 여부
     * @param size 조회할 개수
     * @return 전체 공지사항 목록 DTO
     */
    ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId, boolean onlyImportant, Integer size);

    /**
     * 특정 회원이 가입한 모든 클럽의 최신 소식을 조회합니다.
     * 홈 화면 피드를 구성할 때 사용됩니다.
     *
     * @param memberId 조회할 회원의 ID
     * @param size 조회할 개수
     * @return 모든 클럽의 최신 소식이 통합된 미리보기 DTO
     */
    ClubResponseDTO.MemberNoticeListDTO getNoticeForHome(String memberId, Long cursorId, boolean onlyImportant, Integer size);

    /**
     * ClubQueryService
     * 공지사항(투표 포함)의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param noticeId 조회할 공지사항 ID
     * @return 공지사항 상세 정보 DTO
     */
    ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId, String tag, String memberId);

    /**
     * ClubBookRecommendQueryService
     * 모임의 추천 책 목록을 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param cursorId 페이징 커서 ID
     * @return 추천 책 목록 DTO
     */
    ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId, String memberId);

    /**
     * ClubBookRecommendQueryService
     * 추천 책의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param bookRecommendId 추천 책 ID
     * @return 추천 책 상세 정보 DTO
     */
    ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(Long clubId, Long bookRecommendId, String memberId);

    /**
     * ClubMeetingQueryService
     * [책장] 모임의 책장 목록을 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param cursorId 페이징 커서 ID
     * @param size 조회할 개수
     * @param generation 미팅 기수 (1기, 2기 등)
     * @param memberId 요청자 회원 ID
     * @return 책장 목록 DTO
     */
    BookShelfResponseDTO.BookShelfListDTO getBookShelfList(Long clubId, Long cursorId, Integer size, Integer generation, String memberId);

    /**
     * ClubMeetingQueryService
     * [책장] 책장의 상세 정보를 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @return 책장 상세 정보 DTO
     */
    BookShelfResponseDTO.BookShelfDetailDTO getBookShelfDetail(Long meetingId, String memberId);

    /**
     * ClubMeetingQueryService
     * [책장] 특정 미팅의 전체 발제(토픽) 목록을 조회합니다.(팀 정보 X) (내부용)
     *
     * @param meetingId 미팅 ID
     * @param cursorId 페이징 커서 ID
     * @param size 조회할 개수
     * @param memberId 요청자 회원 ID
     * @return 발제 목록 DTO
     */
    BookShelfResponseDTO.TopicListDTO findTopicsByMeeting(Long meetingId, Long cursorId, Integer size, String memberId);

    /**
     * ClubBookShelfQueryService
     * [책장] 특정 미팅에 대한 한줄평을 size만큼 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @param lastReviewId 마지막으로 조회한 한줄평 ID (무한 스크롤용)
     * @param size 조회할 개수
     * @param memberId 요청자 회원 ID
     * @return 한줄평 목록 DTO
     */
    BookShelfResponseDTO.BookReviewListDTO getBookReviewList(Long meetingId, Long lastReviewId, int size, String memberId);

    /**
     * ClubMeetingQueryService
     * [모임] 모임의 모든 미팅 목록을 조회합니다. (내부용)
     *
     * @param clubId 모임 ID
     * @param cursorId 페이징 커서 ID
     * @param size 조회할 개수
     * @param memberId 요청자 회원 ID
     * @return 미팅 목록 DTO
     */
    MeetingResponseDTO.MeetingListDTO getMeetingsByClub(Long clubId, Long cursorId, Integer size, String memberId);

    /**
     * ClubMeetingQueryService
     * [모임] 특정 미팅의 상세 정보를 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @param memberId 요청자 회원 ID
     * @return 미팅 상세 정보 DTO
     */
    MeetingResponseDTO.MeetingDetailDTO findMeetingDetailById(Long meetingId, String memberId);

    /**
     * ClubMeetingQueryService
     * [모임] 특정 미팅의 발제 목록을 조회합니다.(선택한 팀 정보도 함꼐) (내부용)
     *
     * @param meetingId 미팅 ID
     * @param memberId 요청자 회원 ID
     * @return 미팅 발제 목록 DTO
     */
    List<MeetingResponseDTO.TopicDTO> findMeetingTopicsWithTeam(Long meetingId, String memberId);

    /**
     * ClubMeetingQueryService
     * [모임] 특정 미팅의 팀별 발제를 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @param teamNumber 조회할 팀 번호
     * @param memberId 요청자 회원 ID
     * @return 해당 팀의 정보 DTO
     */
    MeetingResponseDTO.TeamTopicDTO findMeetingTopicsByTeam(Long meetingId, Integer teamNumber, String memberId);

    /**
     * ClubMeetingQueryService
     *
     * [모임] 특정 미팅의 참여 인원 목록을 조회합니다.(내부용) //TODO: 무한스크롤인지 궁금
     *
     * @param meetingId 미팅 ID
     * @param memberId 요청자 회원 ID
     */
    List<MeetingResponseDTO.MeetingMemberDTO> findMeetingMembersByMeeting(Long meetingId, String memberId);

    /**
     * ClubMeetingQueryService
     *
     * [모임] 특정 미팅의 팀별 참여 인원 목록을 조회합니다. (내부용)
     *
     * @param meetingId 미팅 ID
     * @param teamNumber 조회할 팀 번호
     * @param memberId 요청자 회원 ID
     * @return 해당 팀의 참여 인원 목록 DTO
     */
    List<MeetingResponseDTO.MeetingMemberDTO> findTeamMembersByMeeting(Long meetingId, Integer teamNumber, String memberId);

    /**
     * ClubMeetingQueryService
     * 특정 클럽의 모임 캘린더를 조회합니다. (내부용)
     *
     * @param clubId 클럽 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param memberId 요청자 회원 ID
     * @return 미팅 리스트 DTO
     */
    List<MeetingResponseDTO.MeetingInfoDTO> getClubMeetingCalendar(Long clubId, int year, int month, String memberId);

    /**
     * 다른 도메인에서 관계 설정을 위해 엔티티의 프록시(참조)를 조회합니다. (외부용)
     * ‼️ 이 메소드는 실제 DB 조회를 발생시키지 않는 메소드!!!
     * ‼️ 그리고 반드시 외래 키를 설정하는 용도로만 사용되어야 함!
     *
     * 이 메소드는 구현할 때 단순히
     * {@code return clubRepository.getReferenceById(clubId);}만 하면 됨
     *
     * @param clubId 참조할 클럽의 ID
     * @return Club 엔티티의 프록시 객체
     */
    Club findClubReferenceById(Long clubId);
}