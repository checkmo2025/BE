package checkmo.realtime.internal;

import checkmo.realtime.RealtimeAPI;
import checkmo.realtime.RealtimeExternalDTO;
import checkmo.realtime.internal.entity.TeamChatMessage;
import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import checkmo.realtime.internal.repository.TeamChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealtimeAPIImpl implements RealtimeAPI {

    private final TeamChatMessageRepository teamChatMessageRepository;

    @Override
    public RealtimeExternalDTO.TeamChatReportInfo fetchTeamChatReportInfo(Long chatMessageId) {
        TeamChatMessage message = teamChatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new RealtimeException(RealtimeErrorStatus.MESSAGE_NOT_FOUND));

        return RealtimeExternalDTO.TeamChatReportInfo.builder()
                .chatMessageId(message.getId())
                .clubId(message.getClubId())
                .meetingId(message.getMeetingId())
                .teamId(message.getTeamId())
                .build();
    }
}