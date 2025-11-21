package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.Vote;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long>, VoteRepositoryCustom {
    Optional<Vote> findByIdAndClubId(Long id, Long clubId);
}