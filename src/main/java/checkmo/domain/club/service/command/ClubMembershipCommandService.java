package checkmo.domain.club.service.command;

import checkmo.domain.club.dto.club.ClubRequestDTO;
import checkmo.domain.club.dto.club.ClubResponseDTO;

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
     * 독서모임의 가입 신청을 승인합니다.
     *
     * 피그마 참고 페이지 : 피그마 페이지 아직 없음
     *
     * @param clubId 독서모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param ClubMemberId 가입 승인 해줄 멤버의 id DTO (그 멤버 자체의 id 아님!! , ClubMember의 id임)
     */
    void approveJoinRequest(Long clubId, String memberId, Long ClubMemberId);
}
