package checkmo.domain.club.repository;

import checkmo.domain.club.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    Optional<ClubMember> findByClubIdAndMemberId(Long clubId, String memberId);

    @Query("SELECT cm FROM ClubMember cm JOIN cm.club c WHERE cm.memberId = :memberId")
    List<ClubMember> findMyClubInfoByMemberId(@Param("memberId") String memberId);
}
