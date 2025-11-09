package checkmo.clubMeeting.repository;

import checkmo.clubMeeting.entity.BookReview;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewRepository extends JpaRepository<BookReview, Long>, BookReviewRepositoryCustom {
    Optional<BookReview> findByIdAndMeetingId(Long id, Long meetingId);
}
