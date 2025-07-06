package checkmo.domain.club.repository.announcement;

import checkmo.domain.club.entity.announcement.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}
