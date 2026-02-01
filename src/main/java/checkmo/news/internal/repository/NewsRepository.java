package checkmo.news.internal.repository;

import checkmo.news.internal.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long>, NewsQueryRepository {
}