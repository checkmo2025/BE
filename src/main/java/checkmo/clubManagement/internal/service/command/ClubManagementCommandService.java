package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.web.dto.ClubRequestDTO;

/**
 * 독서 모임 자체의 관리를 담당
 */
public interface ClubManagementCommandService {

    /**
     * 독서모임을 생성합니다.
     *
     * @param memberId 사용자 ID
     * @param request  모임 생성 요청 DTO
     * @return 생성된 독서모임 ID
     */
    Long createClub(String memberId, ClubRequestDTO.ClubDetail request);

    /**
     * ClubManagementCommandService 기존 독서 모임 정보를 수정합니다.
     *
     * @param clubId   수정할 모임
     * @param memberId 사용자 ID
     * @param request  모임 수정 요청 정보 DTO
     */
    Long updateClub(Long clubId, String memberId, ClubRequestDTO.ClubDetail request);

    /**
     * 독서 모임을 삭제합니다.
     *
     * @param clubId   삭제할 모임
     * @param memberId 사용자 ID
     */
    void deleteClub(Long clubId, String memberId);
}