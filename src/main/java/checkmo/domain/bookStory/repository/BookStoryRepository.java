package checkmo.domain.bookStory.repository;

import checkmo.domain.bookStory.entity.BookStory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookStoryRepository extends JpaRepository<BookStory, Long> {
}
