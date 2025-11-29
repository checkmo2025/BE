package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.BookRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRecommendRepository extends JpaRepository<BookRecommend, Long>, BookRecommendRepositoryCustom {
}
