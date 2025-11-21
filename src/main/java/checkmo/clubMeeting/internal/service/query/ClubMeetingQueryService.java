package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import java.util.List;
import java.util.Set;

/**
 * 독서 모임의 미팅 조회 서비스
 */
public interface ClubMeetingQueryService {

    /**
     * 독서 모임의 모든 미팅을 조회합니다.
     *
     * @param clubId   독서 모임 ID
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null)
     * @param size     조회할 미팅 개수
     * @return 조회한 미팅 리스트
     */
    List<Meeting> findMeetingsByClubAndCursor(Long clubId, Long cursorId, Integer size);

    /**
     * 독서모임의 모임의 특정 연도와 달에 따라 조회합니다.
     *
     * @param clubId   독서클럽 ID
     * @param year     조회하고자 하는 연도
     * @param month    조회하고자 하는 달
     * @param memberId 요청자 회원 ID
     * @return Meeting 리스트
     */
    List<Meeting> getClubMeetingByYearAndMonth(Long clubId, int year, int month, String memberId);

    /**
     * 모임을 책장 리스트로 조회
     *
     * @param clubId     독서 클럽 ID
     * @param generation 기수 (최신 기수의 모임을 조회하려면 null)
     * @param cursorId   커서 ID (페이징을 위한 커서, 처음에는 null
     * @param size       조회할 미팅 개수
     * @return Meeting 리스트
     */
    List<Meeting> getBookShelfList(Long clubId, Integer generation, Long cursorId, Integer size);

    /**
     * 여러 미팅 ID로 미팅 리스트를 배치 조회합니다.
     *
     * @param meetingIds 미팅 ID 리스트
     * @return 미팅 리스트
     */
    List<Meeting> getMeetingsByIds(Set<Long> meetingIds);

    /**
     * 독서모임이 존재하는지 확인합니다.
     *
     * @param meetingId 미팅 ID
     * @return Meeting 존재하는 미팅 객체
     * @throws ClubMeetingException 미팅이 존재하지 않을 경우
     */
    Meeting validateMeeting(Long meetingId) throws ClubMeetingException;
}
