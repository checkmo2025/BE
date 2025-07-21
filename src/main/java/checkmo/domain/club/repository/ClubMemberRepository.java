package checkmo.domain.club.repository;

import checkmo.domain.club.entity.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    boolean existsByClubIdAndMemberId(Long clubId, String memberId);

    boolean existsByClubIdAndMemberIdAndClubMemberStatus(Long clubId, String memberId, ClubMember.ClubMemberStatus clubMemberStatus);
}
