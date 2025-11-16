package checkmo.clubNotice.internal.service.command;

import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreatedEvent;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;

/**
 * 독서 모임의 커뮤니케이션 관련 기능을 처리 예를 들어, 공지사항과 투표 생성, 투표 참여 등 모임 내 소통 기능을 담당
 */
public interface ClubNoticeCommandService {

    /**
     * 독서 모임에 공지사항을 작성합니다. (모임이랑 연결되지 않은 순수 공지사항)
     *
     * @param clubId   독서모임 ID
     * @param memberId 작성자(운영진) 회원 ID
     * @param request  공지사항 작성 요청 DTO
     * @return 작성된 공지사항
     */
    Notice createPureNotice(Long clubId, String memberId, ClubNoticeRequestDTO.CreateClubNotice request);

    /**
     * 독서 모임의 공지사항을 삭제합니다. (모임이랑 연결되지 않은 순수 공지사항)
     *
     * @param clubId   독서 모임 ID
     * @param noticeId 삭제할 공지사항
     * @param memberId 요청자(운영진) 회원 ID
     */
    void deletePureNotice(Long clubId, Long noticeId, String memberId);

    /**
     * 독서 모임의 모임 생성 시 공지사항을 작성합니다.
     *
     * @param event 모임 생성 이벤트
     */
    void createMeetingNotice(ClubMeetingCreatedEvent event);

    /**
     * 독서 모임에 투표를 생성합니다.
     *
     * @param clubId   독서모임 ID
     * @param memberId 작성자(운영진) 회원 ID
     * @param request  투표 생성 요청 DTO
     * @return 생성된 투표
     */
    Vote createVote(Long clubId, String memberId, ClubNoticeRequestDTO.CreateClubVote request);

    /**
     * 독서 모임에 투표를 삭제합니다.
     *
     * @param clubId   독서 모임 ID
     * @param voteId   삭제할 투표 ID
     * @param memberId 요청자(운영진) 회원 ID
     */
    void deleteVote(Long clubId, Long voteId, String memberId);

    /**
     * 독서 모임의 투표에 참여합니다.
     *
     * @param clubId   독서 모임 ID
     * @param voteId   투표 ID
     * @param memberId 참여자 회원 ID
     * @param request  투표 내역 DTO
     * @return 참여한 투표 ID
     */
    Long haveVote(Long clubId, Long voteId, String memberId, ClubNoticeRequestDTO.VoteResult request);
}
