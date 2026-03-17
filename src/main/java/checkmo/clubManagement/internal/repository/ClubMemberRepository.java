package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.ClubMember;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.repository.projection.ClubIdAndName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Optional<ClubMember> findByClubIdAndMemberId(Long clubId, String memberId);

    Optional<ClubMember> findByIdAndClubId(Long id, Long clubId);

    @Query("SELECT c.id AS id, c.name AS name "
            + "FROM ClubMember cm JOIN cm.club c "
            + "WHERE cm.memberId = :memberId "
            + "AND cm.clubMemberStatus IN :statuses "
            + "ORDER BY cm.id ASC")
    List<ClubIdAndName> findClubIdAndNameByMemberIdAndStatuses(String memberId, EnumSet<ClubMemberStatus> statuses);

    @Query("SELECT cm FROM ClubMember cm "
            + "WHERE cm.club.id = :clubId "
            + "AND cm.clubMemberStatus IN :statuses "
            + "ORDER BY cm.joinedAt DESC, cm.id DESC")
    Page<ClubMember> findClubIdAndStatusesInOrderByDesc(Long clubId, EnumSet<ClubMemberStatus> statuses, Pageable pageable);

    @Query("SELECT cm FROM ClubMember cm "
            + "WHERE cm.club.id = :clubId "
            + "AND (:cursorId IS NULL OR cm.id < :cursorId) "
            + "AND (:statuses IS NULL OR cm.clubMemberStatus IN :statuses) "
            + "ORDER BY cm.id DESC")
    List<ClubMember> findByClubIdAndStatuses(Long clubId, EnumSet<ClubMemberStatus> statuses, Long cursorId, Pageable pageable);

    List<ClubMember> findAllByMemberIdAndClubIdIn(String memberId, List<Long> clubIds);
}
