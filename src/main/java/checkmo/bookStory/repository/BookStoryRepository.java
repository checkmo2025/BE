package checkmo.bookStory.repository;

import checkmo.bookStory.entity.BookStory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookStoryRepository extends JpaRepository<BookStory, Long>, BookStoryQueryRepository {
}
