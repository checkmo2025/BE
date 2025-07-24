package checkmo.domain.bookStory.repository;

import checkmo.domain.bookStory.entity.BookStoryLiked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookStoryLikedRepository extends JpaRepository<BookStoryLiked, Long> {

    /**
     * 특정 사용자가 BookStory를 좋아요했는지 여부 확인
     */
    boolean existsByMemberIdAndBookStoryId(String memberId, Long bookStoryId);

    Optional<BookStoryLiked> findBookStoryLikedByBookStoryIdAndMemberId(Long bookStoryId, String memberId);
}
