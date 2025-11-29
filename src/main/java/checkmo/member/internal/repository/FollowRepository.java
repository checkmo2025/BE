package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Follow;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    boolean existsByFollow(String followerId, String followingId);

    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    void deleteByFollow(String followerId, String followingId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :memberId")
    List<String> getFollowingMemberIds(String memberId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId AND f.following.id IN :memberIds")
    Set<String> findFollowingIdsByFollowerId(String followerId, List<String> memberIds);
}
