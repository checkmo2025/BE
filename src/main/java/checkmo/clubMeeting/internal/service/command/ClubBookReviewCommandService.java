package checkmo.clubMeeting.internal.service.command;

import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;

public interface ClubBookReviewCommandService {

    /**
     * 독서모임의 한줄평을 작성합니다.
     *
     * @param meetingId 미팅 ID
     * @param memberId  작성자 회원 ID
     * @param request   한줄평 내용 DTO (내용 + 평점)
     * @return 생성한 한줄평 ID
     */
    Long createBookReview(Long meetingId, String memberId, BookShelfRequestDTO.BookReviewCreate request);

    /**
     * 독서모임의 한줄평을 수정합니다.
     *
     * @param meetingId 미팅 ID
     * @param reviewId  한줄평 ID
     * @param memberId  요청자 회원 ID
     * @param request   한줄평 내용 DTO (내용 + 평점)
     */
    Long updateBookReview(Long meetingId, Long reviewId, String memberId, BookShelfRequestDTO.BookReviewCreate request);

    /**
     * 독서모임의 한줄평을 삭제합니다.
     *
     * @param meetingId 미팅 ID
     * @param reviewId  한줄평
     * @param memberId  요청자 회원 ID
     */
    void deleteBookReview(Long meetingId, Long reviewId, String memberId);

}
