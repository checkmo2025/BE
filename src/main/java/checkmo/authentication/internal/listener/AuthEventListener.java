package checkmo.authentication.internal.listener;

import checkmo.authentication.internal.service.command.AuthUserCommandService;
import checkmo.member.MemberEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthEventListener {

    private final AuthUserCommandService authUserCommandService;

    @ApplicationModuleListener
    public void handleMemberProfileCompleted(MemberEvent.MemberProfileCompleted event) {
        try {
            authUserCommandService.completeProfile(event.memberId());
        } catch (Exception e) {
            //TODO : 로직 추가
        }
    }
}
