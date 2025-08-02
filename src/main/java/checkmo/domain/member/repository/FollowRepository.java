package checkmo.domain.member.repository;

import checkmo.domain.member.entity.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    @Modifying
    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);

    List<Follow> findByFollowingIdOrderByIdDesc(String memberId, Pageable pageable);

    List<Follow> findByFollowerIdOrderByIdDesc(String memberId, Pageable pageable);

    List<Follow> findByFollowingIdAndIdLessThanOrderByIdDesc(String memberId, Long cursorId, Pageable pageable);

    List<Follow> findByFollowerIdAndIdLessThanOrderByIdDesc(String memberId, Long cursorId, Pageable pageable);


}
