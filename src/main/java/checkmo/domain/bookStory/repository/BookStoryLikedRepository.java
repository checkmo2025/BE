package checkmo.domain.bookStory.repository;

import checkmo.domain.bookStory.entity.BookStoryLiked;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookStoryLikedRepository extends JpaRepository<BookStoryLiked, Long> {
}
