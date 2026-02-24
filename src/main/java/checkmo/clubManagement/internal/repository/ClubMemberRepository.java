package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.repository.projection.ClubIdAndName;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Optional<ClubMember> findByClubIdAndMemberId(Long clubId, String memberId);

    Optional<ClubMember> findByIdAndClubId(Long id, Long clubId);

    @Query("SELECT c.id AS id, c.name AS name "
            + "FROM ClubMember cm JOIN cm.club c "
            + "WHERE cm.memberId = :memberId "
            + "AND cm.clubMemberStatus IN :statuses "
            + "ORDER BY cm.id ASC")
    List<ClubIdAndName> findClubIdAndNameByMemberIdAndStatuses(String memberId, EnumSet<ClubMemberStatus> statuses);

    List<ClubMember> findAllByMemberIdAndClubIdIn(String memberId, List<Long> clubIds);

    @Query("SELECT cm FROM ClubMember cm "
            + "WHERE cm.club.id = :clubId "
            + "AND (:cursorId IS NULL OR cm.id < :cursorId) "
            + "AND (:statuses IS NULL OR cm.clubMemberStatus IN :statuses) "
            + "ORDER BY cm.id DESC")
    List<ClubMember> findByClubIdAndStatuses(
            Long clubId, EnumSet<ClubMemberStatus> statuses, Long cursorId, Pageable pageable);
}
