package checkmo.domain.club.service.command;

import checkmo.domain.club.web.dto.club.ClubRequestDTO;

public interface ClubBookRecommendCommandService {

    /**
     * 독서모임에 책을 추천합니다.
     *
     * @param clubId 독서모임 ID
     * @param memberId 추천하는 회원 ID -> 클럽 회원인지 확인하는 로직 필요
     * @param request 추천할 책 정보 DTO
     * @return 추천한 책의 ID
     */
    Long recommendBook(Long clubId, String memberId, ClubRequestDTO.CreateBookRecommendDTO request);

    /**
     * 독서모임에 추천 책을 수정합니다.
     *
     * @param clubId 독서모임 ID
     * @param memberId 추천하는 회원 ID -> 클럽 회원인지 확인하는 로직 필요
     * @param bookRecommendId 수정할 추천 책의 ID
     * @param request 수정할 추천책의 정보 DTO
     * @return 수정한 추천 책의 ID
     */
    Long updateBookRecommend(Long clubId, String memberId, Long bookRecommendId, ClubRequestDTO.UpdateBookRecommendDTO request);

    /**
     * 독서모임에서 추천한 책을 삭제합니다.
     *
     * @param clubId 독서모임 ID
     * @param memberId 삭제하는 회원 ID -> 클럽 회원인지 확인하는 로직 필요
     * @param bookRecommendId 삭제할 추천 책의 ID
     */
    void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId);
}
