package checkmo.domain.club.repository.announcement;

import checkmo.domain.club.entity.announcement.MemberVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberVoteRepository extends JpaRepository<MemberVote, Long> {
}
