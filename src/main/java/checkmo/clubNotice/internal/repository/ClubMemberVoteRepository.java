package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.ClubMemberVote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberVoteRepository extends JpaRepository<ClubMemberVote, Long> {
    List<ClubMemberVote> findAllByVoteId(Long voteId);

    Optional<ClubMemberVote> findByVoteIdAndClubMemberId(Long voteId, Long clubMemberId);

    void deleteByVoteIdAndClubMemberId(Long voteId, Long clubMemberId);
}
