package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.CommentImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentImageRepository extends JpaRepository<CommentImage, Long> {

    @Query("""
            SELECT image.imageUrl
            FROM CommentImage image
            WHERE image.comment.bookStory.id = :bookStoryId
            """)
    List<String> findImageUrlsByBookStoryId(@Param("bookStoryId") Long bookStoryId);
}
