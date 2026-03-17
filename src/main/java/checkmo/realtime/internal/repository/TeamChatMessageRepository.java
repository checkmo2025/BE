package checkmo.realtime.internal.repository;

import checkmo.realtime.internal.entity.TeamChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamChatMessageRepository extends JpaRepository<TeamChatMessage, Long> {
    List<TeamChatMessage> findByClubIdAndMeetingIdAndTeamIdOrderByIdDesc(
            Long clubId, Long meetingId, Long teamId, Pageable pageable
    );

    List<TeamChatMessage> findByClubIdAndMeetingIdAndTeamIdAndIdLessThanOrderByIdDesc(
            Long clubId, Long meetingId, Long teamId, Long cursorId, Pageable pageable
    );
}
