package checkmo.clubNotice.internal.service.query;

import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ClubNoticeQueryService {

    /**
     * 순수 공지사항(일반 공지) 조회
     *
     * @param clubId   클럽 ID
     * @param noticeId 공지사항 ID
     * @return 공지사항 엔티티
     */
    Notice getNotice(Long clubId, Long noticeId);

    /**
     * 투표 조회
     *
     * @param clubId 클럽 ID
     * @param voteId 투표 ID
     * @return 투표 엔티티
     */
    Vote getVote(Long clubId, Long voteId);

    /**
     * 특정 투표의 모든 투표 내역 조회
     *
     * @param voteId 투표 ID
     * @return 투표 내역 리스트
     */
    List<ClubMemberVote> getMemberVotesByVoteId(Long voteId);

    /**
     * 특정 회원의 투표 내역 조회
     *
     * @param voteId       투표 ID
     * @param clubMemberId 클럽 회원 ID
     * @return 회원의 투표 내역 (없으면 null)
     */
    ClubMemberVote getMyVote(Long voteId, Long clubMemberId);

    /**
     * 클럽의 공지사항 리스트 조회
     *
     * @param clubId        클럽 ID
     * @param onlyImportant 중요 공지만 조회 여부
     * @param cursorId      커서 ID
     * @param pageable      페이징 정보
     * @return 공지사항 리스트
     */
    List<Notice> getNoticeList(Long clubId, boolean onlyImportant, Long cursorId, Pageable pageable);

    /**
     * 클럽의 투표 리스트 조회
     *
     * @param clubId        클럽 ID
     * @param onlyImportant 중요 투표만 조회 여부
     * @param cursorId      커서 ID
     * @param pageable      페이징 정보
     * @return 투표 리스트
     */
    List<Vote> getVoteList(Long clubId, boolean onlyImportant, Long cursorId, Pageable pageable);

    /**
     * 공지사항을 검증합니다.
     *
     * @param clubId   공지사항이 게시된 독서 클럽 ID
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
