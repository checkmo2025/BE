package checkmo.realtime.internal.interceptor;

import checkmo.realtime.internal.exception.RealtimeErrorStatus;
import checkmo.realtime.internal.exception.RealtimeException;
import checkmo.realtime.internal.service.AuthorizationService;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 팀 웹소켓 SUBSCRIBE/SEND 메시지에 대한 권한 체크 인터셉터
 *
 * @ auth(null) 또는 팀 발제/채팅 인가 실패 => command Error 연결 종료
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeamAuthorizationInterceptor implements ChannelInterceptor {
    private static final PathPatternParser PARSER = new PathPatternParser();
    private static final PathPattern SUB_PATTERN
            = PARSER.parse("/sub/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/**");
    private static final PathPattern PUB_PATTERN
            = PARSER.parse("/pub/clubs/{clubId}/meetings/{meetingId}/teams/{teamId}/**");

    private static final String USER_ERRORS = "/user/queue/errors";

    private final AuthorizationService authorizationService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageType simpType = (SimpMessageType) message.getHeaders().get("simpMessageType");
        if (simpType == SimpMessageType.HEARTBEAT) {
            return message;
        }

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (command != StompCommand.SUBSCRIBE && command != StompCommand.SEND) {
            return message; // 구독과 발행 메시지만 권한 체크
        }

        Authentication auth = (accessor.getUser() instanceof Authentication a) ? a : null;
        if (auth == null || !auth.isAuthenticated()) {
            throw new RealtimeException(RealtimeErrorStatus.UNAUTHENTICATED);
        }

        String destination = accessor.getDestination();
        if (!StringUtils.hasText(destination)) {
            throw new RealtimeException(RealtimeErrorStatus.MISSING_DESTINATION);
        }

        if (command == StompCommand.SUBSCRIBE && USER_ERRORS.equals(destination)) {
            return message;
        }

        DestinationVariables vars = extractVariables(destination);

        authorizationService.authorizeTeamAccess(vars.clubId, vars.meetingId, vars.teamId, auth.getName());
        return message;
    }

    private DestinationVariables extractVariables(String destination) {
        PathContainer path = PathContainer.parsePath(destination);
        Map<String, String> variables;
        if (SUB_PATTERN.matches(path)) {
            variables = Objects.requireNonNull(SUB_PATTERN.matchAndExtract(path)).getUriVariables();
        } else if (PUB_PATTERN.matches(path)) {
            variables = Objects.requireNonNull(PUB_PATTERN.matchAndExtract(path)).getUriVariables();
        } else {
            // SecurityConfig에서 이미 denyAll로 막혀야 정상인데 여기까지 왔다면...
            throw new RealtimeException(RealtimeErrorStatus.INVALID_DESTINATION);
        }

        try {
            return new DestinationVariables(
                    Long.parseLong(variables.get("clubId")),
                    Long.parseLong(variables.get("meetingId")),
                    Long.parseLong(variables.get("teamId"))
            );
        } catch (Exception e) {
            throw new RealtimeException(RealtimeErrorStatus.INVALID_DESTINATION);
        }
    }

    private record DestinationVariables(Long clubId, Long meetingId, Long teamId) {
    }
}
