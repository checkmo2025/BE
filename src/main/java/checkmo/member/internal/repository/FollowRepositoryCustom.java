package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Follow;
import java.util.List;

public interface FollowRepositoryCustom {

    List<Follow> findFollowers(String followingId, Long cursorId, int pageSize, List<String> excludedMemberIds);

    List<Follow> findFollowings(String followerId, Long cursorId, int pageSize, List<String> excludedMemberIds);
}
