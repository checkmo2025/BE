package checkmo.domain.club.service.query;

import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

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

    /**
     * 클럽의 모든 공지와 투표를 조회합니다.
     *
     * @param clubId 클럽 ID
     * @param onlyImportant 중요 공지/투표만 조회할지 여부
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return 공지와 투표 목록 DTO
     */
    List<ClubResponseDTO.NoticeItem> getAllNoticesAndVotes(Long clubId, boolean onlyImportant, Long cursorId, Pageable pageable);

    /**
     * 회원이 가입한 클럽의 모든 공지와 투표를 조회합니다.
     *
     * @param onlyImportant 중요 공지/투표만 조회할지 여부
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @return 공지와 투표 목록 DTO
     */
    List<ClubResponseDTO.NoticeItem> getMemberNoticesAndVotes(String memberId, boolean onlyImportant, Long cursorId, Pageable pageable);

}
