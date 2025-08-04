package checkmo.domain.member.repository;

import checkmo.domain.member.entity.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    @Modifying
    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);

    List<Follow> findByFollowingIdOrderByIdDesc(String memberId, Pageable pageable);

    List<Follow> findByFollowerIdOrderByIdDesc(String memberId, Pageable pageable);

    List<Follow> findByFollowingIdAndIdLessThanOrderByIdDesc(String memberId, Long cursorId, Pageable pageable);

    List<Follow> findByFollowerIdAndIdLessThanOrderByIdDesc(String memberId, Long cursorId, Pageable pageable);

    /**
     * 배치로 팔로우 관계 확인 - N+1 문제 해결용
     * @param followerId 팔로우하는 사람 ID
     * @param memberIds 확인할 대상 회원 ID 목록
     * @return followerId가 팔로우하고 있는 회원 ID 집합
     */
    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :followerId AND f.followingId IN :memberIds")
    Set<String> findFollowingIdsByFollowerId(@Param("followerId") String followerId, @Param("memberIds") List<String> memberIds);
}
