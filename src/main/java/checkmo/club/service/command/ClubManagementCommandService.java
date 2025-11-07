package checkmo.club.service.command;

import checkmo.club.entity.Club;
import checkmo.club.entity.ClubMember;
import checkmo.club.web.dto.club.ClubRequestDTO;

/**
 * 독서 모임 자체의 생성 모임 자체의 관리를 담당
 */
public interface ClubManagementCommandService {

    /**
     * 독서모임을 생성합니다.
     * <p>
     * 피그마 참고 페이지 : #독서모임 - 모임 생성하기 첫화면 첫스크롤
     *
     * @param clubMember 모임 생성 요청한 회원
     * @param request    모임 생성 요청 DTO
     * @return 생성된 독서모임 ID
     */
    Long createClub(ClubMember clubMember, ClubRequestDTO.ClubDetailDTO request);

    /**
     * ClubManagementCommandService 기존 독서 모임 정보를 수정합니다.
     *
     * @param club       수정할 모임
     * @param clubMember 수정 요청한 회원
     * @param request    모임 수정 요청 정보 DTO
     */
    void updateClub(Club club, ClubMember clubMember, ClubRequestDTO.ClubDetailDTO request);
}