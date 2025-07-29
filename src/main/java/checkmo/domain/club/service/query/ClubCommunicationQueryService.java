package checkmo.domain.club.service.query;

import checkmo.domain.club.web.dto.club.ClubResponseDTO;

public interface ClubCommunicationQueryService {

    /**
     * 공지 or 투표 상세 조회
     *
     * @param clubId   클럽 ID
     * @param itemId   공지 또는 투표 ID
     * @param tag      "공지", "모임", "투표" 중 하나
     * @return 공통된 NoticeItem DTO
     */
    ClubResponseDTO.ClubNoticeDetailDTO getNoticeOrVoteDetail(Long clubId, Long itemId, String tag, String memberId);
}
