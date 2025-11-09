package checkmo.clubManagement.repository;

import checkmo.clubManagement.entity.ClubCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubCategoryRepository extends JpaRepository<ClubCategory, Long> {
    // 클럽 ID로 ClubCategory 리스트 조회
    List<ClubCategory> findByClubId(Long clubId);

    List<ClubCategory> findByClubIdIn(List<Long> clubIds);

}