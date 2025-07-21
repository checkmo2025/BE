package checkmo.domain.club.service.command;

import checkmo.domain.club.web.dto.club.ClubRequestDTO;

/**
 * 독서 모임 자체의 생성, 수정, 삭제 등등
 * 모임 자체의 관리를 담당
 */
public interface ClubManagementCommandService {

    /**
     * 독서모임을 생성합니다.
     * <p>
     * 피그마 참고 페이지 : #독서모임 - 모임 생성하기 첫화면 첫스크롤
     *
     * @param request 모임 생성 요청 DTO
     * @return 생성된 독서모임 ID
     */
    Long createClub(String memberId, ClubRequestDTO.ClubDetailDTO request);

    /**
     * 입력된 모임 이름의 중복 여부를 확인합니다.
     *
     * @param clubName 확인할 모임 이름
     * @return true: 중복됨 / false: 사용 가능
     */
    boolean isClubNameDuplicate(String clubName);
}
