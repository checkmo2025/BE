package checkmo.domain.club.service.command;

import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;

/**
 * 독서모임의 가입 신청, 승인, 탈퇴, 권한 변경 등등
 * 독서모임 내부의 멤버십 관련 작업을 처리
 */
public interface ClubMembershipCommandService {

    /**
     * 독서모임에 가입 신청을 합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 검색하기 - 특정 모임 가입 신청하기 클릭시
     *
     * @param clubId 독서모임 ID
     * @param memberId 회원 ID -> 로그인한 회원의 ID를 사용
     * @param request 가입 신청 메시지 DTO
     * @return 가입 신청 후의 독서모임 정보 DTO : 공개인 경우는 ID 사용해 프론트에서 리다이렉트.
     */
    ClubResponseDTO.ClubInfoDTO joinClub(Long clubId, String memberId, ClubRequestDTO.ClubMemberJoinDTO request);

    /**
     * ClubMembershipCommandService
     * 독서 모임 회원의 등급(상태/역할)을 수정합니다.
     *
     * @param clubId          독서 모임 ID
     * @param targetMemberId  수정 대상 회원 ID
     * @param currentMemberId 요청자(운영진) 회원 ID
     * @param status 수정할 등급 (MEMBER, STAFF, PENDING, BLOCKED 중 선택)
     * @return 수정된 회원의 응답 DTO
     */
    ClubResponseDTO.ClubMemberDTO updateClubMemberStatus(Long clubId, Long targetMemberId, String currentMemberId, String status);

    /**
     * 독서 모임에서 탈퇴합니다.
     *
     * @param clubId   독서 모임 ID
     * @param memberId 탈퇴할 회원 ID (본인)
     */
    void leaveClub(Long clubId, String memberId);

}
