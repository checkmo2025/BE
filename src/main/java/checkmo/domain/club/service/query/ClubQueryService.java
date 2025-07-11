package checkmo.domain.club.service.query;

import checkmo.domain.club.dto.club.ClubResponseDTO;

/**
 * 독서 클럽 조회 서비스
 *
 * 독서 클럽 자체에 대한 조회 기능을 담당
 * ex) 독서 클럽 목록 조회, 검색 기능, 특정 독서 클럽 상세 정보 조회 등을 처리
 */
public interface ClubQueryService {
    /**
     * 독서 클럽 목록을 조회합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 검색하기
     *
     * @param keyword 검색 키워드 (모임명 등)
     * @param region 지역 필터 (0: 지역 필터 선택 안함 / 1: 지역 필터 선택해서 검색 키워드로 지역명도 검색 가능)
     * @param participants 지역 필터 (0: 동아리 대상별 검색 필터 선택 안함 / 1: 동아리 대상별 검색 필터 선택해서 검색 키워드로 동아리 대상도 검색 가능)
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return 독서 클럽 목록 DTO
     */
    ClubResponseDTO.ClubListDTO getClubList(String keyword, int region, int participants, Long cursorId);

    /**
     * 내가 가입한 독서 클럽 목록을 전체 조회합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 내 모임 바로가기
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    ClubResponseDTO.MyClubListDTO getMyClubList(String memberId);

    /**
     * 내가 가입한 독서 클럽 목록을 size 개수만큼 조회합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 내 모임 바로가기
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @param size 조회할 개수
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    ClubResponseDTO.MyClubListDTO getMyClubList(String memberId, int size);

    /**
     * 독서모임의 멤버 리스트를 조회합니다. (상태별로 조회 가능)
     *
     * 피그마 참고 페이지 : #
     *
     * @param clubId 독서모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param clubMemberStatus 검색할 회원들의 상태 (예: "MEMBER", "STAFF", "PENDING", "BLOCKED")
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return 검색한 독서모임 멤버 목록 DTO (승인 안된 상태의 독서 모임 멤버도 포함될 수 있음)
     */
    ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId);

    /**
     * 독서모임의 상세 정보를 조회합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 검색하기 - 특정 모임 클릭시
     *
     * @param clubId 독서모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @return 독서 클럽 상세 정보 DTO
     */
    ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId);

    /**
     * 독서모임의 이름 중복 여부를 확인 합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 생성하기 첫화면 첫스크롤
     *
     * @param clubName 독서모임 이름
     * @return 중복 여부 (true: 중복, false: 중복 아님)
     */
    boolean isDuplicateClubName(String clubName);

    /**
     * 중요 공지사항 최신순 size 개수만큼 조회 (최상단에 들어갈 공지사항들)
     *
     * 피그마 참고 페이지 : #독서모임(운영진) - 공지사항 홈화면
     *
     * @param clubId 독서모임 ID
     * @param memberId 회원의 ID   -> 해당 독서 클럽에만 포함되면 전부 조회 가능
     * @param size 조회할 개수
     * @return 중요 공지사항 목록 DTO
     */
    ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, int size);

    /**
     * 공지사항 최신순으로 전체 조회
     *
     * 피그마 참고 페이지 : #독서모임(운영진) - 공지사항 홈화면
     *
     * @param clubId 독서모임 ID
     * @param memberId 회원의 ID   -> 해당 독서 클럽에만 포함되면 전부 조회 가능
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return 전체 공지사항 목록 DTO
     */
    ClubResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId);

    /**
     * 공지사항 상세 보기
     *
     * 피그마 참고 페이지 : #독서모임(운영진) - 공지사항 홈화면 - 공지사항 클릭시
     *
     * @param clubId 독서모임 ID
     * @param noticeId 공지사항 ID
     */
    ClubResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId);
}
