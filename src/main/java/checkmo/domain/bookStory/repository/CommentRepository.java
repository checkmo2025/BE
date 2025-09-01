package checkmo.domain.bookStory.repository;

import checkmo.domain.bookStory.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c " +
            "FROM Comment c " +
            "LEFT JOIN FETCH c.childrenComment " +
            "WHERE c.bookStoryId = :bookStoryId " +
                    "AND c.parentComment IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findParentComments(@Param("bookStoryId") Long bookStoryId);
}
