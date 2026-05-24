package checkmo.clubMeeting.internal.service.query;

import checkmo.clubMeeting.internal.entity.BookReview;
import checkmo.clubMeeting.internal.exception.ClubMeetingErrorStatus;
import checkmo.clubMeeting.internal.exception.ClubMeetingException;
import checkmo.clubMeeting.internal.repository.BookReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubBookReviewQueryService {

    private final BookReviewRepository bookReviewRepository;

    public List<BookReview> retrieveBookReviews(Long meetingId, Long cursorId, Integer size) {
        return bookReviewRepository.findBookReviewsByCusor(meetingId, cursorId, size); // int로 암묵적 언박싱
    }

    public BookReview validateBookReview(Long reviewId) throws ClubMeetingException {
        return bookReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.BOOK_REVIEW_NOT_FOUND));
    }

    public BookReview validateBookReview(Long reviewId, Long meetingId) throws ClubMeetingException {
        return bookReviewRepository.findByIdAndMeetingId(reviewId, meetingId)
                .orElseThrow(() -> new ClubMeetingException(ClubMeetingErrorStatus.BOOK_REVIEW_NOT_FOUND));
    }
}
