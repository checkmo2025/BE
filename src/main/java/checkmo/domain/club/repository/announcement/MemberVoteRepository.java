package checkmo.domain.club.repository.announcement;

import checkmo.domain.club.entity.announcement.MemberVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberVoteRepository extends JpaRepository<MemberVote, Long> {
    List<MemberVote> findAllByVoteId(Long voteId);
    Optional<MemberVote> findByVoteIdAndMemberId(Long voteId, String memberId);
}
