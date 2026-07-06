package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberBlock;
import java.util.List;
import java.util.Optional;

public interface MemberBlockRepositoryCustom {

    List<MemberBlock> findBlocks(Long blockerId, Long cursorId, int pageSize);

    List<Long> findBlockedMemberIds(Long blockerId);

    List<Long> findBlockRelatedMemberIds(Long memberId);

    Optional<MemberBlock> findBetween(Long memberId1, Long memberId2);

    boolean existsBetween(Long memberId1, Long memberId2);
}
