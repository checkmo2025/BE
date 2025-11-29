package checkmo.book.internal.repository;

import checkmo.book.internal.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {
}
