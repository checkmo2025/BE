package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.BookRecommend;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRecommendRepository extends JpaRepository<BookRecommend, Long> {
    List<BookRecommend> findTop10ByClubMember_Club_IdAndIdLessThanOrderByIdDesc(Long clubId, Long id);

    boolean existsByClubMember_Club_IdAndIdLessThan(Long clubId, Long id);
}
