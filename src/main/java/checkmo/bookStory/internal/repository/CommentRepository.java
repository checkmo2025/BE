package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT DISTINCT c " +
            "FROM Comment c " +
            "LEFT JOIN FETCH c.childrenComment " +
            "WHERE c.bookStory.id = :bookStoryId " +
            "AND c.parentComment IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findParentComments(@Param("bookStoryId") Long bookStoryId);

    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true, c.deletedAt = CURRENT_TIMESTAMP WHERE c.memberId = :memberId AND c.deleted = false")
    void softDeleteAllByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.bookStory.id = :bookStoryId AND c.parentComment IS NOT NULL")
    void deleteChildCommentsByBookStoryId(@Param("bookStoryId") Long bookStoryId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.bookStory.id = :bookStoryId AND c.parentComment IS NULL")
    void deleteParentCommentsByBookStoryId(@Param("bookStoryId") Long bookStoryId);
}
