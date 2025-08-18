package checkmo.domain.member.repository;

import checkmo.domain.member.entity.MemberCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCategoryRepository extends JpaRepository<MemberCategory, String> {

    List<MemberCategory> findByMemberId(String memberId);
}