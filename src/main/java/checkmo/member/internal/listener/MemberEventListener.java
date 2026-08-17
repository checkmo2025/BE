package checkmo.member.internal.listener;

import checkmo.authentication.AuthenticationEvent;
import checkmo.member.internal.service.MemberFacade;
import checkmo.member.internal.service.command.MemberCommandService;
import checkmo.member.internal.service.command.TermsAgreementCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class MemberEventListener {

    private final MemberCommandService memberCommandService;
    private final MemberFacade memberFacade;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void createMember(AuthenticationEvent.CreateMember event) {
        memberFacade.createMember(
                event.id(),
                event.legacyId(),
                event.email(),
                toTermsAgreementCommands(event.agreements()),
                event.requireRequiredTermsAgreement()
        );
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void reactivateMember(AuthenticationEvent.ReactivateMember event) {
        memberCommandService.reactivateIfDeactivated(event.id());
    }

    private List<TermsAgreementCommand> toTermsAgreementCommands(
            List<AuthenticationEvent.TermsAgreement> agreements
    ) {
        if (agreements == null) {
            return List.of();
        }

        return agreements.stream()
                .map(agreement -> new TermsAgreementCommand(
                        agreement.termsId(),
                        agreement.agreed()
                ))
                .toList();
    }
}
