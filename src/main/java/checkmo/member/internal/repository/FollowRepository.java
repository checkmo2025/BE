package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Follow;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {

    long countByFollowing_Id(Long followingId);

    long countByFollower_Id(Long followerId);

    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    boolean existsByFollow(Long followerId, Long followingId);

    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    void deleteByFollow(Long followerId, Long followingId);

    @Modifying
    @Query("""
            DELETE FROM Follow f
            WHERE (f.follower.id = :memberId1 AND f.following.id = :memberId2)
               OR (f.follower.id = :memberId2 AND f.following.id = :memberId1)
            """)
    void deleteBetweenMembers(Long memberId1, Long memberId2);

    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :memberId OR f.following.id = :memberId")
    void deleteAllByMemberId(Long memberId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :memberId")
    List<Long> getFollowingMemberIds(Long memberId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId AND f.following.id IN :memberIds")
    Set<Long> findFollowingIdsByFollowerId(Long followerId, List<Long> memberIds);
}
