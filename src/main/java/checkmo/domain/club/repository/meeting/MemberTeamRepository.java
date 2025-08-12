package checkmo.domain.club.repository.meeting;

import checkmo.domain.club.entity.meeting.MemberTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberTeamRepository extends JpaRepository<MemberTeam, Long> {
    @Query("SELECT mt " +
            "FROM MemberTeam mt " +
            "JOIN FETCH mt.clubMember cm " +
            "WHERE mt.teamId = :teamId " +
            "ORDER BY cm.memberId ASC")
    List<MemberTeam> findAllWithClubMemberByTeamId(Long teamId);
}
