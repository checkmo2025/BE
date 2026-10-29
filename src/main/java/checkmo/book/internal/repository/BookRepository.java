package checkmo.book.internal.repository;

import checkmo.book.internal.entity.Book;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface BookRepository extends JpaRepository<Book, String> {

    @Modifying
    @Query(value = """
            INSERT INTO book (id, title, author, publisher, description, img_url, likes, created_at, updated_at)
            VALUES (:#{#book.id}, :#{#book.title}, :#{#book.author}, :#{#book.publisher},
                    :#{#book.description}, :#{#book.imgUrl}, 0, :now, :now)
            ON DUPLICATE KEY UPDATE id = id
            """, nativeQuery = true)
    int insertIfAbsent(@Param("book") Book book, @Param("now") LocalDateTime now);

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
