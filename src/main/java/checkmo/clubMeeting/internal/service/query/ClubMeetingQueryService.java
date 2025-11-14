package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;
import java.util.Set;

/**
 * 독서 모임의 미팅 조회 서비스
 */
public interface ClubMeetingQueryService {

    /**
     * 독서 모임의 모든 미팅을 조회합니다. (책장 아님) - 이 페이지에서는 발제 작성 불가 및, 한줄평 조회 불가
     * <p>
     * 피그마 참고 페이지 : #독서모임 - 모임 생성 후, 모임 리스트
     *
     * @param clubId   독서 모임 ID
     * @param cursorId 커서 ID (페이징을 위한 커서, 처음에는 null)
     * @param size     조회할 미팅 개수
     * @return 조회한 미팅 리스트
     */
    List<Meeting> findMeetingsByClubAndCursor(Long clubId, Long cursorId, Integer size);

    /**
     * 독서모임의 모임 캘린더를 조회합니다.
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
     * <p>
     * 피그마 참고 페이지 : #독서모임(사용자) - 책장 홈화면
     *
     * @param clubId     독서 클럽 ID
     * @param generation 기수 (최신 기수의 모임을 조회하려면 null)
     * @param cursorId   커서 ID (페이징을 위한 커서, 처음에는 null 또는 0)
     * @param size       조회할 미팅 개수
     * @return Meeting 리스트
     */
    List<Meeting> getBookShelfList(Long clubId, Integer generation, Long cursorId, Integer size);

    /**
     * 여러 미팅 ID로 미팅 리스트를 조회합니다.
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
     * @throws GeneralException 미팅이 존재하지 않을 경우
     */
    Meeting validateMeeting(Long meetingId) throws GeneralException;
}
