package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Follow;
import java.util.List;

public interface FollowRepositoryCustom {

    List<Follow> findFollowers(Long followingId, Long cursorId, int pageSize, List<Long> excludedMemberIds);

    List<Follow> findFollowings(Long followerId, Long cursorId, int pageSize, List<Long> excludedMemberIds);
}
