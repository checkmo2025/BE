package checkmo.book.internal.repository;

import checkmo.book.internal.entity.BookLiked;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookLikedRepository extends JpaRepository<BookLiked, Long> {

    Optional<BookLiked> findByBook_IdAndMemberId(String bookId, Long memberId);

    @Query("SELECT bl.book.id FROM BookLiked bl WHERE bl.memberId = :memberId AND bl.book.id IN :bookIds")
    List<String> findLikedBookIds(@Param("memberId") Long memberId, @Param("bookIds") List<String> bookIds);

    default Set<String> findLikedBookIdSet(Long memberId, List<String> bookIds) {
        if (memberId == null || bookIds == null || bookIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(findLikedBookIds(memberId, bookIds));
    }

    @EntityGraph(attributePaths = "book")
    List<BookLiked> findByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    @EntityGraph(attributePaths = "book")
    List<BookLiked> findByMemberIdAndIdLessThanOrderByIdDesc(Long memberId, Long cursorId, Pageable pageable);
}
