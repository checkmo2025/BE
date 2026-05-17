package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberBlock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long>, MemberBlockRepositoryCustom {

    boolean existsByBlocker_IdAndBlocked_Id(String blockerId, String blockedId);

    Optional<MemberBlock> findByBlocker_IdAndBlocked_Id(String blockerId, String blockedId);

    @Modifying
    @Query("DELETE FROM MemberBlock mb WHERE mb.blocker.id = :memberId OR mb.blocked.id = :memberId")
    void deleteAllByMemberId(String memberId);
}
