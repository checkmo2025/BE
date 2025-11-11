package checkmo.clubMeeting.internal.service.command;

import checkmo.clubMeeting.web.dto.bookshelf.BookShelfRequestDTO;

/**
 * 독서모임의 '한줄평'에 대한 비즈니스 요구사항을 수행합니다.
 */
public interface ClubBookReviewCommandService {

    /**
     * 독서모임의 한줄평을 작성합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 추가 하기
     *
     * @param meetingId 미팅 ID
     * @param memberId  작성자 회원 ID
     * @param request   한줄평 내용 DTO (내용 + 평점)
     * @return 생성한 한줄평 ID
     */
    Long createBookReview(Long meetingId, String memberId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * 독서모임의 한줄평을 수정합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 등록시
     *
     * @param meetingId 미팅 ID
     * @param reviewId  한줄평 ID
     * @param memberId  요청자 회원 ID
     * @param request   한줄평 내용 DTO (내용 + 평점)
     */
    Long updateBookReview(Long meetingId, Long reviewId, String memberId, BookShelfRequestDTO.BookReviewDTO request);

    /**
     * 독서모임의 한줄평을 삭제합니다.
     * <p>
     * 피그마 참고 페이지: #독서모임(사용자) - 책장 [특정 책]에서 한줄평 및 평점 등록시
     *
     * @param meetingId 미팅 ID
     * @param reviewId  한줄평
     * @param memberId  요청자 회원 ID
     */
    void deleteBookReview(Long meetingId, Long reviewId, String memberId);
    
}
