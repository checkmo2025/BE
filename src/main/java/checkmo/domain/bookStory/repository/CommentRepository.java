package checkmo.domain.bookStory.repository;

import checkmo.domain.bookStory.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
