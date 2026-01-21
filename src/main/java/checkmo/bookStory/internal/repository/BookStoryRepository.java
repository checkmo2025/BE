package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.BookStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookStoryRepository extends JpaRepository<BookStory, Long>, BookStoryQueryRepository {

    @Modifying
    @Query("UPDATE BookStory b SET b.viewCount = b.viewCount + :viewCount WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long bookStoryId, @Param("viewCount") int viewCount);
}
