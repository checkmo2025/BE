package checkmo.member.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender javaMailSender;

    // 이메일 발송 메서드
    @Async
    public void sendEmail(String email, String verificationCode) {
        try {
            // 이메일 메시지 생성
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email); // 받는 사람 이메일
            message.setSubject("책모 회원가입 인증번호"); // 이메일 제목
            message.setText("인증번호: " + verificationCode + "\n\n" + "인증번호는 10분간 유효합니다."); // 이메일 본문
            javaMailSender.send(message); // 이메일 발송
            log.info("이메일 발송 성공: {}", email);
        } catch (Exception e) {
            log.error("이메일 발송 실패: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}