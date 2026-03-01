package checkmo.book.internal.repository;

import checkmo.book.internal.entity.BookLiked;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookLikedRepository extends JpaRepository<BookLiked, Long> {

    @Query("SELECT COUNT(bl) > 0 FROM BookLiked bl WHERE bl.memberId = :memberId AND bl.book.id = :bookId")
    boolean existsByMemberIdAndBookId(@Param("memberId") String memberId, @Param("bookId") String bookId);

    @Query("SELECT bl FROM BookLiked bl WHERE bl.book.id = :bookId AND bl.memberId = :memberId")
    Optional<BookLiked> findByBookAndMember(@Param("bookId") String bookId, @Param("memberId") String memberId);

    @Query("SELECT bl.book.id FROM BookLiked bl WHERE bl.memberId = :memberId AND bl.book.id IN :bookIds")
    List<String> findLikedBookIds(@Param("memberId") String memberId, @Param("bookIds") List<String> bookIds);

    @EntityGraph(attributePaths = "book")
    List<BookLiked> findByMemberIdOrderByIdDesc(String memberId, Pageable pageable);

    @EntityGraph(attributePaths = "book")
    List<BookLiked> findByMemberIdAndIdLessThanOrderByIdDesc(String memberId, Long cursorId, Pageable pageable);
}
