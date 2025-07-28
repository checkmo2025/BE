package checkmo.domain.category.repository;

import checkmo.domain.category.entity.MemberCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCategoryRepository extends JpaRepository<MemberCategory, String> {

    List<MemberCategory> findByMemberId(String memberId);
}
