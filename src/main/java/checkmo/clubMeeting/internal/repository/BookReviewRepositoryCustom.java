package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.BookReview;
import java.util.List;

public interface BookReviewRepositoryCustom {
    /**
     * 독서 모임의 한줄평 리스트를 커서 기반으로 조회합니다.
     *
     * @param meetingId    조회하고자 하는 독서모임 ID
     * @param lastReviewId 마지막으로 조회한 한줄평 ID (무한 스크롤용, 처음에는 null)
     * @param size         조회할 한줄평 개수
     * @return 한줄평 리스트
     */
    public List<BookReview> findBookReviewsByCusor(Long meetingId, Long lastReviewId, int size);
}
