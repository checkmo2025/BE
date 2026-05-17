package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberBlock;
import java.util.List;
import java.util.Optional;

public interface MemberBlockRepositoryCustom {

    List<MemberBlock> findBlocks(String blockerId, Long cursorId, int pageSize);

    List<String> findBlockedMemberIds(String blockerId);

    List<String> findBlockRelatedMemberIds(String memberId);

    Optional<MemberBlock> findBetween(String memberId1, String memberId2);

    boolean existsBetween(String memberId1, String memberId2);
}
