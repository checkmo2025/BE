package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.MemberTeam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberTeamRepository extends JpaRepository<MemberTeam, Long> {
    @Query("SELECT mt " +
            "FROM MemberTeam mt " +
            "WHERE mt.teamId IN :teamIds " +
            "ORDER BY mt.memberId ASC")
    List<MemberTeam> findAllWithClubMemberByTeamIds(List<Long> teamIds);
}
