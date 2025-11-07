package checkmo.member.repository;

import checkmo.member.entity.MemberCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCategoryRepository extends JpaRepository<MemberCategory, String> {

    List<MemberCategory> findByMemberId(String memberId);
}