package checkmo.infra.email.internal.listener;

import checkmo.authentication.AuthenticationEvent;
import checkmo.infra.email.internal.service.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class EmailEventListener {

    private final EmailSender emailSender;

    @ApplicationModuleListener
    public void handleEmailEvent(AuthenticationEvent.SendVerificationEmail event) {
        try {
            emailSender.sendVerificationEmail(event.email(), event.verificationCode());
        } catch (Exception e) {
            log.error("이메일 전송 실패, SendVerificationEmail: {}", event, e);
            throw e;
        }
    }
}
