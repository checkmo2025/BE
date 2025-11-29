package checkmo.member.internal.listener;

import checkmo.authentication.AuthenticationEvent;
import checkmo.member.internal.service.command.MemberCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class MemberEventListener {

    private final MemberCommandService memberCommandService;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void createMember(AuthenticationEvent.CreateMember event) {
        memberCommandService.createMember(event.id(), event.email());
    }
}
