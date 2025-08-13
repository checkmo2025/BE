package checkmo.domain.club.repository;

import checkmo.domain.club.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long>, ClubMemberRepositoryCustom {
    Optional<ClubMember> findByClubIdAndMemberId(Long clubId, String memberId);

    Optional<ClubMember> findByClubIdAndId(Long clubId, Long id);

    @Query("SELECT c.id, c.name FROM ClubMember cm JOIN cm.club c WHERE cm.memberId = :memberId")
    List<Object[]> findClubIdAndNameByMemberId(@Param("memberId") String memberId);

    List<ClubMember> findAllByMemberIdAndClubIdIn(String memberId, List<Long> clubIds);

    @Query("SELECT cm FROM ClubMember cm WHERE cm.club.id = :clubId AND cm.memberId IN :memberIds")
    List<ClubMember> findClubMembersByClubIdAndMemberIdIn(Long clubId, List<String> memberIds);
}
