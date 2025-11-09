package checkmo.clubNotice.internal.facade;

import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;

/**
 * Club Notice Command Facade
 * <p>
 * Club 공지사항 및 투표 관련 모든 Command(공지사항 작성/삭제, 투표 생성/삭제/참여) 관련 서비스들을 통합적으로 제공하는 Facade 입니다.
 */
public interface ClubNoticeCommandFacade {

    /**
     * ClubNoticeCommandService 모임에 공지사항을 작성합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 작성자(운영진) 회원 ID
     * @param request  공지사항 작성 요청 DTO
     * @return 생성된 공지사항 id
     */
    ClubNoticeResponseDTO.ClubNoticeDetailDTO createPureNotice(Long clubId, String memberId,
                                                               ClubNoticeRequestDTO.CreateClubNoticeDTO request); //

    /**
     * ClubNoticeCommandService 모임의 공지사항을 삭제합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param noticeId 삭제할 공지사항 ID
     */
    void deletePureNotice(Long clubId, String memberId, Long noticeId); //

    /**
     * ClubNoticeCommandService 모임에 투표를 생성합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 작성자(운영진) 회원 ID
     * @param request  투표 생성 요청 DTO
     * @return 생성된 투표 ID
     */
    Long createVote(Long clubId, String memberId, ClubNoticeRequestDTO.CreateClubVoteDTO request);

    /**
     * ClubNoticeCommandService 모임의 투표를 삭제합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 요청자(운영진) 회원 ID
     * @param voteId   삭제할 투표 ID
     */
    void deleteVote(Long clubId, String memberId, Long voteId); //

    /**
     * ClubNoticeCommandService 모임의 투표에 참여합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param memberId 참여자 회원 ID
     * @param voteId   투표 ID
     * @param request  투표 선택 항목 DTO
     * @return 투표 ID
     */
    Long haveVote(Long clubId, String memberId, Long voteId, ClubNoticeRequestDTO.VoteResultDTO request);

}
