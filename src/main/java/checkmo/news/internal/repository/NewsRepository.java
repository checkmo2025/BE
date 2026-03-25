package checkmo.news.internal.repository;

import checkmo.news.internal.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long>, NewsQueryRepository {
    Page<News> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

}