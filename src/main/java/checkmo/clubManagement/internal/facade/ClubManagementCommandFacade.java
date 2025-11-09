package checkmo.clubManagement.internal.facade;

import checkmo.clubManagement.web.dto.ClubRequestDTO;
import checkmo.clubManagement.web.dto.ClubResponseDTO;

/**
 * Club Management Command Facade
 * <p>
 * Club 운영에 대한 모든 Command(클럽 생성/수정, 클럽 회원 생성/수정/삭제, 책 추천 생성/수정/삭제) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface ClubManagementCommandFacade {

    /**
     * ClubManagementCommandService 새로운 독서 모임을 생성합니다. (내부용)
     *
     * @param memberId 생성자 회원 ID
     * @param request  모임 생성 요청 정보 DTO
     * @return 생성된 독서 모임의 상세 정보 DTO
     */
    Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request); //

    /**
     * ClubManagementCommandService 기존 독서 모임 정보를 수정합니다. (내부용)
     *
     * @param clubId   수정할 모임 ID
     * @param memberId 수정 요청한 회원 ID
     * @param request  모임 수정 요청 정보 DTO
     */
    void updateClub(Long clubId, String memberId, ClubRequestDTO.ClubDetailDTO request);

    /**
     * ClubMembershipCommandService 독서 모임에 가입을 신청합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 신청자 회원 ID
     * @param request  가입 신청 메시지 DTO
     * @return 가입 신청 후의 모임 정보 DTO
     */
    Long joinClub(Long clubId, String memberId, ClubRequestDTO.ClubMemberJoinDTO request); //

    /**
     * ClubMemberCommandService 독서 모임 회원의 등급(상태/역할)을 수정합니다. (내부용)
     *
     * @param clubId             독서 모임 ID
     * @param actorId            요청자(운영진) 회원 ID
     * @param targetClubMemberId 수정 대상 회원 ID
     * @param status             수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
     * @return 수정된 회원의 응답 DTO
     */
    ClubResponseDTO.ClubMemberUpdateResponseDTO updateClubMemberStatus(Long clubId, String actorId,
                                                                       Long targetClubMemberId, String status);

    /**
     * ClubMembershipCommandService 독서 모임에서 탈퇴합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 탈퇴할 회원 ID
     */
    void leaveClub(Long clubId, String memberId);


    /**
     * ClubBookRecommendCommandService 모임에 책을 추천합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 추천자 회원 ID
     * @param request  추천 책 정보 DTO
     * @return 추천된 책의 상세 정보 DTO
     */
    ClubResponseDTO.BookRecommendDetailDTO recommendBook(Long clubId, String memberId,
                                                         ClubRequestDTO.CreateBookRecommendDTO request);

    /**
     * ClubBookRecommendCommandService 추천한 책 정보를 수정합니다. (내부용)
     *
     * @param clubId          모임 ID
     * @param memberId        요청자 회원 ID
     * @param bookRecommendId 수정할 추천 책 ID
     * @param request         수정할 정보 DTO
     * @return 수정된 책의 상세 정보 DTO
     */
    ClubResponseDTO.BookRecommendDetailDTO updateBookRecommend(Long clubId, String memberId, Long bookRecommendId,
                                                               ClubRequestDTO.UpdateBookRecommendDTO request);

    /**
     * ClubBookRecommendCommandService 추천한 책을 삭제합니다. (내부용)
     *
     * @param clubId          모임 ID
     * @param memberId        요청자 회원 ID
     * @param bookRecommendId 삭제할 추천 책 ID
     */
    void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId);

}
