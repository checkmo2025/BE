package checkmo.realtime.internal.service;

import checkmo.common.template.CursorPagingHelper;
import checkmo.common.template.CursorResult;
import checkmo.member.MemberAPI;
import checkmo.member.MemberExternalDTO.BasicInfo;
import checkmo.realtime.internal.entity.TeamChatMessage;
import checkmo.realtime.internal.repository.TeamChatMessageRepository;
import checkmo.realtime.web.dto.ChatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatQueryService {

    private static final int PAGE_SIZE = 30;

    private final TeamChatMessageRepository teamChatMessageRepository;
    private final MemberAPI memberAPI;

    @Transactional(readOnly = true)
    public ChatResponseDTO.ChatHistoryList fetchHistory(
            Long clubId, Long meetingId, Long teamId, Long cursorId
    ) {
        CursorResult<TeamChatMessage> page = CursorPagingHelper.getPage(
                (size) -> getDescTeamChatMessage(clubId, meetingId, teamId, cursorId, size),
                TeamChatMessage::getId,
                PAGE_SIZE
        );

        List<TeamChatMessage> descContent = page.content();
        List<TeamChatMessage> ascContent = new ArrayList<>(descContent);
        Collections.reverse(ascContent); // 과거 -> 최신으로 reverse

        Map<Long, BasicInfo> memberInfoMap = getMemberInfoMap(ascContent);

        List<ChatResponseDTO.Chat> chats = ascContent.stream()
                .map(m -> {
                    BasicInfo sender = memberInfoMap.get(m.getSenderMemberId());
                    return ChatResponseDTO.Chat.builder()
                            .sender(sender)
                            .messageId(m.getId())
                            .content(m.getContent())
                            .sendAt(m.getSentAt())
                            .build();
                })
                .toList();

        return ChatResponseDTO.ChatHistoryList.builder()
                .chats(chats)
                .hasNext(page.hasNext())
                .nextCursor(page.nextCursor())
                .build();
    }

    private List<TeamChatMessage> getDescTeamChatMessage(
            Long clubId, Long meetingId, Long teamId, Long cursorId, int size
    ) {
        PageRequest pageable = PageRequest.of(0, size);
        if (cursorId == null) {
            return teamChatMessageRepository.findByClubIdAndMeetingIdAndTeamIdOrderByIdDesc(clubId, meetingId, teamId, pageable);
        }
        return teamChatMessageRepository.findByClubIdAndMeetingIdAndTeamIdAndIdLessThanOrderByIdDesc(clubId, meetingId, teamId, cursorId, pageable);
    }

    private Map<Long, BasicInfo> getMemberInfoMap(List<TeamChatMessage> teamChatMessages) {
        List<Long> memberIds = teamChatMessages.stream()
                .map(TeamChatMessage::getSenderMemberId)
                .distinct()
                .toList();
        return memberAPI.fetchMemberBasicInfoByMemberIds(toMemberApiIds(memberIds)).entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Long.valueOf(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    private List<String> toMemberApiIds(List<Long> memberIds) {
        return memberIds.stream()
                .map(String::valueOf)
                .toList();
    }
}
