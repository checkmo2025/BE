package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClubMemberTeamRepository extends JpaRepository<ClubMemberTeam, Long> {
    @Query("SELECT mt " +
            "FROM ClubMemberTeam mt " +
            "WHERE mt.team.id IN :teamIds " +
            "ORDER BY mt.clubMemberId ASC")
    List<ClubMemberTeam> findAllByTeamIds(List<Long> teamIds);
}
