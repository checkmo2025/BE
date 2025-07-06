package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
}
