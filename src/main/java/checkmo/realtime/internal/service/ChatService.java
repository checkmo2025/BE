package checkmo.realtime.internal.service;

import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO.BasicInfo;
import checkmo.realtime.internal.entity.TeamChatMessage;
import checkmo.realtime.internal.event.RealtimeEvent;
import checkmo.realtime.internal.repository.TeamChatMessageRepository;
import checkmo.realtime.web.dto.ChatResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MemberAPI memberAPI;
    private final TeamChatMessageRepository teamChatMessageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void saveTeamChatMessage(
            Long clubId, Long meetingId, Long teamId,
            String senderMemberId, String content
    ) {
        teamChatMessageRepository.save(
                TeamChatMessage.builder()
                        .clubId(clubId)
                        .meetingId(meetingId)
                        .teamId(teamId)
                        .senderMemberId(senderMemberId)
                        .content(content)
                        .sentAt(LocalDateTime.now())
                        .build()
        );

        BasicInfo basicInfo = memberAPI.fetchMemberBasicInfo(senderMemberId);
        ChatResponseMessage message = ChatResponseMessage.builder()
                .clubId(clubId)
                .meetingId(meetingId)
                .teamId(teamId)
                .senderMemberId(senderMemberId)
                .senderNickname(basicInfo.getNickname())
                .senderProfileImageUrl(basicInfo.getProfileImageUrl())
                .content(content)
                .sendAt(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(new RealtimeEvent.TeamChatSavedEvent(clubId, meetingId, teamId, message));
    }
}
