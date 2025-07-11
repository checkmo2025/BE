package checkmo.domain.club.service.command;

import checkmo.domain.club.dto.club.ClubRequestDTO;

/**
 * 독서 모임의 커뮤니케이션 관련 기능을 처리
 * 예를 들어, 공지사항과 투표 생성, 투표 참여 등 모임 내 소통 기능을 담당
 */
public interface ClubCommunicationCommandService {

    /**
     * 독서 모임에 공지사항을 작성합니다. (모임이랑 연결되지 않은 순수 공지사항)
     *
     * @param clubId 독서 모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param request 공지사항 작성 요청 DTO
     * @return 작성된 공지사항 ID
     */
    Long createNotice(Long clubId, String memberId, ClubRequestDTO.CreateClubNoticeDTO request);

    /**
     * 독서 모임의 공지사항을 삭제합니다.
     *
     * @param clubId   독서 모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param noticeId 삭제할 공지사항 ID
     */
    void deleteNotice(Long clubId, String memberId, Long noticeId);

    /**
     * 독서 모임에 투표를 생성합니다.
     *
     * @param clubId 독서 모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param request 투표 생성 요청 DTO
     * @return 생성된 투표 ID
     */
    Long createVote(Long clubId, String memberId, ClubRequestDTO.CreateClubVoteDTO request);

    /**
     * 독서 모임에 투표를 삭제합니다.
     *
     * @param clubId 독서 모임 ID
     * @param memberId 운영진 ID -> 운영진인지 확인하는 로직 필요 ClubMember에서 Role 확인 -> 어노테이션으로 처리 고려
     * @param voteId 삭제할 투표 ID
     */
    void deleteVote(Long clubId, String memberId, Long voteId);

    /**
     * 독서 모임의 투표에 참여합니다.
     *
     * @param clubId 독서 모임 ID
     * @param memberId 참여자 회원 ID -> 독서 클럽의 회원인지만 확인
     * @param voteId 투표 ID
     * @param request 투표 내역 DTO
     * @return 참여한 투표 ID
     */
    Long participateInPoll(Long clubId, String memberId, Long voteId, ClubRequestDTO.VoteResultDTO request);
}
