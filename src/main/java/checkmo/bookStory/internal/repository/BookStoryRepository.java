package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.BookStory;
import checkmo.bookStory.internal.repository.projection.BookStoryPrevNextProjection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookStoryRepository extends JpaRepository<BookStory, Long>, BookStoryQueryRepository {

    Optional<BookStory> findByIdAndDeletedFalse(Long id);

    @Modifying
    @Query("UPDATE BookStory b SET b.viewCount = b.viewCount + :viewCount WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long bookStoryId, @Param("viewCount") int viewCount);

    @Query("""
                SELECT
                    (SELECT MAX(b1.id) FROM BookStory b1 WHERE b1.memberId = :memberId AND b1.id < :id AND b1.deleted = false) AS prevId,
                    (SELECT MIN(b2.id) FROM BookStory b2 WHERE b2.memberId = :memberId AND b2.id > :id AND b2.deleted = false) AS nextId
                FROM BookStory b
                WHERE b.id = :id
            """)
    BookStoryPrevNextProjection findPrevNextBookStoryId(@Param("memberId") String memberId, @Param("id") Long bookStoryId);

    @Modifying
    @Query("UPDATE BookStory b SET b.deleted = true, b.deletedAt = CURRENT_TIMESTAMP WHERE b.memberId = :memberId AND b.deleted = false")
    void softDeleteAllByMemberId(@Param("memberId") String memberId);
}
