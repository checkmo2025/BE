package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c " +
            "FROM Comment c " +
            "LEFT JOIN FETCH c.childrenComment " +
            "WHERE c.bookStory.id = :bookStoryId " +
            "AND c.parentComment IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findParentComments(@Param("bookStoryId") Long bookStoryId);
}
