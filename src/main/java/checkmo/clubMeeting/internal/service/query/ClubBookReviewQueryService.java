package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.common.apiPayload.exception.GeneralException;
import java.util.List;

/**
 * 독서 모임의 한줄평 관련 조회 서비스
 */
public interface ClubBookReviewQueryService {

    /**
     * 독서 모임의 책장(한줄평) 리스트를 조회합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책] 클릭시
     *
     * @param meetingId 미팅 ID
     * @param cursorId  마지막으로 조회한 한줄평 ID (무한 스크롤용, 처음에는 null 또는 0)
     * @param size      조회할 한줄평 개수
     * @return 조회한 한줄평 리스트
     */
    List<BookReview> findBookReviewsByMeeting(Long meetingId, Long cursorId, Integer size);

    /**
     * 독서모임의 한줄평이 존재하는지 확인합니다.
     *
     * @param reviewId  한줄평 ID
     * @param meetingId 미팅 ID
     * @return BookReview 존재하는 한줄평 객체
     */
    BookReview validateBookReview(Long reviewId, Long meetingId) throws GeneralException;
}
