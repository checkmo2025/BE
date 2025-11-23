package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Follow;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    @Modifying
    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);

    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :memberId")
    List<String> getFollowingMemberIds(String memberId);

    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :followerId AND f.followingId IN :memberIds")
    Set<String> findFollowingIdsByFollowerId(String followerId, List<String> memberIds);
}
