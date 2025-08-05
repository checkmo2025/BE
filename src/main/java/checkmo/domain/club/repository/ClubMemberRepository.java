package checkmo.domain.club.repository;

import checkmo.domain.club.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Optional<ClubMember> findByClubIdAndMemberId(Long clubId, String memberId);

    Optional<ClubMember> findByClubIdAndId(Long clubId, Long id);

    @Query("SELECT c.id, c.name FROM ClubMember cm JOIN cm.club c WHERE cm.memberId = :memberId")
    List<Object[]> findClubIdAndNameByMemberId(@Param("memberId") String memberId);

    List<ClubMember> findTop10ByClub_IdAndClubMemberStatusAndIdLessThanOrderByIdDesc(Long clubId, ClubMember.ClubMemberStatus status, Long cursorId);
    List<ClubMember> findTop10ByClub_IdAndIdLessThanOrderByIdDesc(Long clubId, Long cursorId);

    boolean existsByClub_IdAndClubMemberStatusAndIdLessThan(Long clubId, ClubMember.ClubMemberStatus status, Long lastId);
    boolean existsByClub_IdAndIdLessThan(Long clubId, Long lastId);
}
