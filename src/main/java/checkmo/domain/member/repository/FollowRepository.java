package checkmo.domain.member.repository;

import checkmo.domain.member.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    @Modifying
    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);
}
