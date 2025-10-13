package checkmo.domain.club.service.query;

import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.announcement.Vote;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClubNoticeQueryService {

    /**
     * 공지 or 투표 상세 조회
     *
     * @param clubId 클럽 ID
     * @param itemId 공지 또는 투표 ID
     * @param tag "공지", "모임", "투표" 중 하나
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
    List<ClubResponseDTO.ClubNoticeWithClubDTO> getMemberNoticesAndVotes(String memberId, boolean onlyImportant, Long cursorId, Pageable pageable);

    /**
     * 공지사항을 검증합니다.
     *
     * @param clubId 공지사항이 게시된 독서 클럽 ID
     * @param noticeId 검증할 공지사항 ID
     * @return 공지사항 객체
     */
    Notice validateNotice(Long clubId, Long noticeId) throws GeneralException;

    /**
     * 투표를 검증합니다.
     *
     * @param clubId 투표가 게시된 독서 클럽 ID
     * @param voteId 검증할 투표 ID
     * @return 투표 객체
     */
    Vote validateVote(Long clubId, Long voteId) throws GeneralException;
}
