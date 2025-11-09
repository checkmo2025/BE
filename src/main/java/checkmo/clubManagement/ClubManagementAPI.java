package checkmo.clubManagement;

import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;

public interface ClubManagementAPI {

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (내부용)
     * <p>
     * 피그마 참고 페이지 : #독서모임 - 내 모임 바로가기
     *
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    ClubResponseDTO.MyClubListDTO getMyClubList(String memberId);

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (내부용)
     * <p>
     * 피그마 참고 페이지 : #마이페이지
     *
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @return 내가 가입한 독서 클럽 목록 DTO
     */
    ClubResponseDTO.MyPageClubListDTO getMyPageClubList(String memberId, Long cursorId, Integer size);

    /**
     * 특정 회원이 가입한 모임 목록을 조회합니다. (외부용) 마이페이지 등 다른 서비스에서 사용됩니다.
     *
     * @param memberId 회원 ID
     * @return 회원이 가입한 모임의 간략한 정보 목록 DTO
     */
    ClubManagementExternalDTO.MyClubList getMyClubListForShare(String memberId);

    /**
     * ClubQueryService 조건에 맞는 독서 모임 목록을 검색합니다. (내부용)
     *
     * @param memberId    요청자 회원 ID (해당 클럽 회원인지 확인용)
     * @param filter      검색 필터 (keyword, name, region, participants)
     * @param pageRequest 페이징 요청 (cursorId, size)
     * @return 검색된 모임 목록 DTO
     */
    ClubResponseDTO.ClubListDTO getClubList(String memberId, ClubRequestDTO.ClubSearchFilter filter,
                                            ClubRequestDTO.CursorPageRequest pageRequest);

    /**
     * ClubQueryService 독서 모임의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId   조회할 모임 ID
     * @param memberId 조회자 회원 ID
     * @return 모임 상세 정보 DTO
     */
    ClubResponseDTO.ClubDetailDTO getClubInfo(Long clubId, String memberId);

    /**
     * ClubQueryService 특정 상태의 모임 회원 목록을 조회합니다. (내부용)
     *
     * @param clubId           모임 ID
     * @param memberId         요청자(운영진) 회원 ID
     * @param clubMemberStatus 조회할 회원 상태
     * @param cursorId         페이징 커서 ID
     * @return 해당 상태의 회원 목록 DTO
     */
    ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus,
                                                                Long cursorId, Integer size);

    /**
     * ClubQueryService 모임 이름의 중복 여부를 확인합니다. (내부용)
     *
     * @param clubName 확인할 모임 이름
     * @return 중복 시 true
     */
    boolean isDuplicateClubName(String clubName);

    /**
     * 특정 회원이 특정 클럽의 스태프인지 확인합니다. (내부용)
     *
     * @param clubId   클럽 ID
     * @param memberId 회원 ID
     * @return 스태프 여부 (true: 스태프, false: 일반 회원)
     */
    Boolean checkStaffStatus(Long clubId, String memberId);

    /**
     * ClubBookRecommendQueryService 모임의 추천 책 목록을 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param cursorId 페이징 커서 ID
     * @return 추천 책 목록 DTO
     */
    ClubResponseDTO.BookRecommendListDTO getRecommendedBooks(Long clubId, Long cursorId, String memberId);

    /**
     * ClubBookRecommendQueryService 추천 책의 상세 정보를 조회합니다. (내부용) Service에서 순수 엔티티 조회 후 Facade에서 DTO 변환 처리
     *
     * @param clubId          모임 ID
     * @param bookRecommendId 추천 책 ID
     * @param memberId        요청자 회원 ID
     * @return 추천 책 상세 정보 DTO
     */
    ClubResponseDTO.BookRecommendDetailDTO getRecommendedBookDetail(Long clubId, Long bookRecommendId, String memberId);
}
