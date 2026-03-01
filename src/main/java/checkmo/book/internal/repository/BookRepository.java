package checkmo.book.internal.repository;

import checkmo.book.internal.entity.Book;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE book b
            SET b.likes = (
                SELECT COUNT(*)
                FROM book_liked bl
                WHERE bl.book_id = b.id
            )
            WHERE b.id = :bookId
            """, nativeQuery = true)
    void syncLikesByBookId(@Param("bookId") String bookId);
}
