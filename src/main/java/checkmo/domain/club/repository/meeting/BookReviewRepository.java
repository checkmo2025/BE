package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReview, Long>, BookReviewRepositoryCustom {
    Optional<BookReview> findByIdAndMeetingId(Long id, Long meetingId);
}
