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
            String subject = (event.type() == AuthenticationEvent.VerificationType.UPDATE_EMAIL)
                ? "[책모] 이메일 변경 인증번호 안내"
                : "[책모] 회원가입 인증번호 안내";

            emailSender.sendVerificationEmail(event.email(), event.verificationCode(), subject);
        } catch (Exception e) {
            log.error("이메일 전송 실패, email: {}", event.email(), e);
            throw e;
        }
    }

    @ApplicationModuleListener
    public void handleTempPasswordEvent(AuthenticationEvent.SendTempPassword event) {
        try {
            emailSender.sendTempPassword(event.email(), event.tempPassword());
        } catch (Exception e) {
            log.error("임시 비밀번호 전송 실패, email: {}", event.email(), e);
            throw e;
        }
    }
}
