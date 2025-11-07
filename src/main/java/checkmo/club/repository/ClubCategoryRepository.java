package checkmo.club.repository;

import checkmo.club.entity.ClubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubCategoryRepository extends JpaRepository<ClubCategory, Long> {
    // 클럽 ID로 ClubCategory 리스트 조회
    List<ClubCategory> findByClubId(Long clubId);

    List<ClubCategory> findByClubIdIn(List<Long> clubIds);

}