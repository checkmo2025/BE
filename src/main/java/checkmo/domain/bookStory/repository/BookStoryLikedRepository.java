package checkmo.domain.bookStory.repository;

import checkmo.domain.bookStory.entity.BookStoryLiked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookStoryLikedRepository extends JpaRepository<BookStoryLiked, Long> {

    /**
     * 특정 사용자가 BookStory를 좋아요했는지 여부 확인
     */
    boolean existsByMemberIdAndBookStoryId(String memberId, Long bookStoryId);

    /**
     * 특정 사용자가 좋아요한 BookStory Id 목록을 조회
     */
    @Query("SELECT bsl.bookStoryId FROM BookStoryLiked bsl WHERE bsl.memberId = :memberId AND bsl.bookStoryId IN :bookStoryIds")
    List<Long> findLikedBookStoryIdsByMemberIdAndBookStoryIds(@Param("memberId") String memberId, @Param("bookStoryIds") List<Long> bookStoryIds);

    Optional<BookStoryLiked> findBookStoryLikedByBookStoryIdAndMemberId(Long bookStoryId, String memberId);
}
