package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.BookStory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookStoryRepository extends JpaRepository<BookStory, Long>, BookStoryQueryRepository {
}
