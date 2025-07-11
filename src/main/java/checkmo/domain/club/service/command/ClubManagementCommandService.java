package checkmo.domain.club.service.command;

import checkmo.domain.club.dto.club.ClubRequestDTO;

/**
 * 독서 모임 자체의 생성, 수정, 삭제 등등
 * 모임 자체의 관리를 담당
 */
public interface ClubManagementCommandService {

    /**
     * 독서모임을 생성합니다.
     *
     * 피그마 참고 페이지 : #독서모임 - 모임 생성하기 첫화면 첫스크롤
     *
     * @param request 모임 생성 요청 DTO
     * @return 생성된 독서모임 ID
     */
    Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request);

    /**
     * 독서모임을 수정합니다.
     *
     * 피그마 참고 페이지 : # 아직 피그마 페이지 없음
     *
     * @param clubId 독서모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param request 모임 수정 요청 DTO
     * @return 생성된 독서모임 ID
     */
    Long updateClub(Long clubId, String memberId, ClubRequestDTO.ClubDetailDTO request);
}
