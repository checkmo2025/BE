package checkmo.clubNotice.internal.repository;

import checkmo.clubNotice.internal.entity.MemberVote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberVoteRepository extends JpaRepository<MemberVote, Long> {
    List<MemberVote> findAllByVoteId(Long voteId);

    Optional<MemberVote> findByVoteIdAndMemberId(Long voteId, String memberId);

    void deleteByVoteIdAndMemberId(Long voteId, String memberId);
}
