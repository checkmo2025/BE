package checkmo.domain.club.repository;

import checkmo.domain.club.entity.BookRecommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRecommendRepository extends JpaRepository<BookRecommend, Long> {
    List<BookRecommend> findTop10ByClubMember_Club_IdAndIdLessThanOrderByIdDesc(Long clubId, Long id);
    boolean existsByClubMember_Club_IdAndIdLessThan(Long clubId, Long id);
}
