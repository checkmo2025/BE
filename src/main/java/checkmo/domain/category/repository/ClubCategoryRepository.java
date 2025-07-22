package checkmo.domain.category.repository;

import java.util.List;
import checkmo.domain.category.entity.ClubCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClubCategoryRepository extends JpaRepository<ClubCategory, Long> {
    // clubId로 연결된 ClubCategory 리스트 조회
    List<ClubCategory> findByClubId(Long clubId);

    // clubId와 categoryId 리스트를 받아 해당하는 ClubCategory들 삭제
    @Modifying
    @Transactional
    @Query("delete from ClubCategory cc where cc.club.id = :clubId and cc.category.id in :categoryIds")
    void deleteByClubIdAndCategoryIds(Long clubId, List<Long> categoryIds);
}
