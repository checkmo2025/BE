package checkmo.bookStory.internal.repository;

import checkmo.bookStory.internal.entity.BookStoryLiked;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookStoryLikedRepository extends JpaRepository<BookStoryLiked, Long> {

    @Query("SELECT COUNT(bsl) > 0 FROM BookStoryLiked bsl WHERE bsl.memberId = :memberId AND bsl.bookStory.id = :bookStoryId")
    boolean existsByMemberIdAndBookStoryId(@Param("memberId") String memberId, @Param("bookStoryId") Long bookStoryId);

    @Query("SELECT bsl.bookStory.id FROM BookStoryLiked bsl WHERE bsl.memberId = :memberId AND bsl.bookStory.id IN :bookStoryIds")
    List<Long> findLikedBookStoryIds(
            @Param("memberId") String memberId,
            @Param("bookStoryIds") List<Long> bookStoryIds
    );

    @Query("SELECT bsl FROM BookStoryLiked bsl WHERE bsl.bookStory.id = :bookStoryId AND bsl.memberId = :memberId")
    Optional<BookStoryLiked> findByBookStoryAndMember(@Param("bookStoryId") Long bookStoryId,
                                                      @Param("memberId") String memberId);

    @Modifying
    @Query("DELETE FROM BookStoryLiked bsl WHERE bsl.bookStory.id = :bookStoryId")
    void deleteByBookStoryId(@Param("bookStoryId") Long bookStoryId);
}
