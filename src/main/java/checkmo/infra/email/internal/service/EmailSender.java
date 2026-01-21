package checkmo.infra.email.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailSender {

    private final JavaMailSender javaMailSender;

    // 이메일 발송 메서드
    public void sendVerificationEmail(String email, String verificationCode) {
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

    public void sendTempPassword(String email, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("책모 임시 비밀번호");
            message.setText("임시 비밀번호: " + tempPassword + "\n\n" + "로그인 후 마이페이지에서 비밀번호를 변경해주세요.");
            javaMailSender.send(message); // 이메일 발송
            log.info("임시 비밀번호 발송 성공: {}", email);
        } catch (Exception e) {
            log.error("임시 비밀번호 발송 실패: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("Failed to send temp password", e);
        }
    }
}
