package checkmo.domain.club.repository;

import checkmo.domain.club.entity.BookRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRecommendRepository extends JpaRepository<BookRecommend, Long> {
}
