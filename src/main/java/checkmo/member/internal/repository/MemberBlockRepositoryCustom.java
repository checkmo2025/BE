package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberBlock;
import java.util.List;

public interface MemberBlockRepositoryCustom {

    List<MemberBlock> findBlocks(String blockerId, Long cursorId, int pageSize);

    List<String> findBlockedMemberIds(String blockerId);

    List<String> findBlockRelatedMemberIds(String memberId);

    boolean existsBetween(String memberId1, String memberId2);
}
