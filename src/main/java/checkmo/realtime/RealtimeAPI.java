package checkmo.realtime;

public interface RealtimeAPI {

    RealtimeExternalDTO.TeamChatReportInfo fetchTeamChatReportInfo(Long chatMessageId);

    String fetchChatSenderMemberId(Long chatMessageId);
}