package checkmo.domain.club.service.command;

import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.announcement.Vote;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;

/**
 * 독서 모임의 커뮤니케이션 관련 기능을 처리
 * 예를 들어, 공지사항과 투표 생성, 투표 참여 등 모임 내 소통 기능을 담당
 */
public interface ClubNoticeCommandService {

    /**
     * 독서 모임에 공지사항을 작성합니다. (모임이랑 연결되지 않은 순수 공지사항)
     *
     * @param club 독서 모임
     * @param clubMember 공지사항 작성 요청자 (운영진인지 확인 필요)
     * @param request 공지사항 작성 요청 DTO
     * @return 작성된 공지사항
     */
    Notice createPureNotice(Club club, ClubMember clubMember, ClubRequestDTO.CreateClubNoticeDTO request);

    /**
     * 독서 모임의 공지사항을 삭제합니다. (모임이랑 연결되지 않은 순수 공지사항)
     *
     * @param clubId 독서 모임 ID
     * @param clubMember 공지사항 삭제 요청자 (운영진인지 확인 필요)
     * @param noticeId 삭제할 공지사항
     */
    void deletePureNotice(Long clubId, ClubMember clubMember, Long noticeId);

    /**
     * 독서 모임에 투표를 생성합니다.
     *
     * @param club 독서 모임
     * @param clubMember 투표 생성 요청자 (운영진인지 확인 필요)
     * @param request 투표 생성 요청 DTO
     * @return 생성된 투표 ID
     */
    Vote createVote(Club club, ClubMember clubMember, ClubRequestDTO.CreateClubVoteDTO request);

    /**
     * 독서 모임에 투표를 삭제합니다.
     *
     * @param clubId 독서 모임 ID
     * @param clubMember 투표 삭제 요청자 (운영진인지 확인 필요)
     * @param voteId 삭제할 투표 ID
     */
    void deleteVote(Long clubId, ClubMember clubMember, Long voteId);

    /**
     * 독서 모임의 투표에 참여합니다.
     *
     * @param clubId 독서 모임 ID
     * @param clubMember 참여자 회원 -> 독서 클럽의 회원인지만 확인
     * @param voteId 투표 ID
     * @param request 투표 내역 DTO
     * @return 참여한 투표 ID
     */
    Long haveVote(Long clubId, ClubMember clubMember, Long voteId, ClubRequestDTO.VoteResultDTO request);
}
