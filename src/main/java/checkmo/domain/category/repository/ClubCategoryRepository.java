package checkmo.domain.category.repository;

import checkmo.domain.category.entity.ClubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubCategoryRepository extends JpaRepository<ClubCategory, Long> {
}
