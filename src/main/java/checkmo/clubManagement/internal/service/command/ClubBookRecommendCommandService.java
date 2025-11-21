package checkmo.clubManagement.internal.service.command;

import checkmo.clubManagement.web.dto.ClubRequestDTO;

public interface ClubBookRecommendCommandService {

    /**
     * 독서모임에 책을 추천합니다.
     *
     * @param clubId   독서모임 ID
     * @param memberId 추천하는 회원 ID
     * @param request  추천할 책 정보 DTO
     * @return 추천한 책의 ID
     */
    Long recommendBook(Long clubId, String memberId, ClubRequestDTO.CreateBookRecommend request);

    /**
     * 독서모임에 추천 책을 수정합니다.
     *
     * @param clubId          독서모임 ID
     * @param memberId        추천하는 회원 ID
     * @param bookRecommendId 수정할 추천 책의 ID
     * @param request         수정할 추천책의 정보 DTO
     * @return 수정한 추천 책의 ID
     */
    Long updateBookRecommend(
            Long clubId,
            String memberId,
            Long bookRecommendId,
            ClubRequestDTO.UpdateBookRecommend request
    );

    /**
     * 독서모임에서 추천한 책을 삭제합니다.
     *
     * @param clubId          독서모임 ID
     * @param memberId        삭제하는 회원 ID
     * @param bookRecommendId 삭제할 추천 책의 ID
     */
    void deleteRecommendedBook(Long clubId, String memberId, Long bookRecommendId);
}
