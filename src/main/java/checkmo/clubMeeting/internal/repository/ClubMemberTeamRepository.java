package checkmo.clubMeeting.internal.repository;

import checkmo.clubMeeting.internal.entity.ClubMemberTeam;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClubMemberTeamRepository extends JpaRepository<ClubMemberTeam, Long> {
    @Query("SELECT mt " +
            "FROM ClubMemberTeam mt " +
            "WHERE mt.team.id IN :teamIds " +
            "ORDER BY mt.clubMemberId ASC")
    List<ClubMemberTeam> findAllByTeamIds(List<Long> teamIds);

    @Query("SELECT cmt FROM ClubMemberTeam cmt " +
            "WHERE cmt.team.id = :teamId " +
            "AND (:cursorId IS NULL OR cmt.clubMemberId > :cursorId) "
            + "ORDER BY cmt.clubMemberId ASC")
    List<ClubMemberTeam> findAllByTeamIdsAndCursorId(Long teamId, Long cursorId, Pageable pageable);
}
