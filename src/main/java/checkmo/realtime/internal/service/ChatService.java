package checkmo.realtime.internal.service;

import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO.BasicInfo;
import checkmo.realtime.web.dto.ChatResponseMessage;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final MemberAPI memberAPI;

    public ChatResponseMessage saveTeamChatMessage(
            Long clubId, Long meetingId, Long teamId,
            String senderMemberId, String content
    ) {
        // TODO: 메시지 저장 로직 구현 (예: 데이터베이스에 저장)
        BasicInfo basicInfo = memberAPI.fetchMemberBasicInfo(senderMemberId);
        return ChatResponseMessage.builder()
                .clubId(clubId)
                .meetingId(meetingId)
                .teamId(teamId)
                .senderMemberId(senderMemberId)
                .senderNickname(basicInfo.getNickname())
                .senderProfileImageUrl(basicInfo.getProfileImageUrl())
                .content(content)
                .sendAt(LocalDateTime.now())
                .build();
    }
}
