package checkmo.domain.club.repository;

import checkmo.domain.club.entity.ClubMember;
import org.springframework.data.domain.Pageable;
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

    List<ClubMember> findByClubIdAndClubMemberStatusAndIdLessThanOrderByIdDesc(Long clubId, ClubMember.ClubMemberStatus status, Long cursorId, Pageable pageable);
    List<ClubMember> findByClubIdAndIdLessThanOrderByIdDesc(Long clubId, Long cursorId, Pageable pageable);

    List<ClubMember> findByClubIdAndClubMemberStatusOrderByIdDesc(Long clubId, ClubMember.ClubMemberStatus status, Pageable pageable);
    List<ClubMember> findByClubIdOrderByIdDesc(Long clubId, Pageable pageable);

    boolean existsByClubIdAndClubMemberStatusAndIdLessThan(Long clubId, ClubMember.ClubMemberStatus status, Long lastId);
    boolean existsByClubIdAndIdLessThan(Long clubId, Long lastId);
}
