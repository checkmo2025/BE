package checkmo.realtime;

public interface RealtimeAPI {

    RealtimeExternalDTO.TeamChatReportInfo fetchTeamChatReportInfo(Long chatMessageId);

    Long fetchChatSenderMemberId(Long chatMessageId);
}
