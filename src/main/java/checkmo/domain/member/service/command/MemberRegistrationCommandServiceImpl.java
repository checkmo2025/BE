package checkmo.domain.member.service.command;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRegistrationCommandServiceImpl implements MemberRegistrationCommandService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender javaMailSender;

    private static final String EMAIL_VERIFICATION_PREFIX = "verification:";
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofMinutes(10); // 10분
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void sendEmailVerification(String email) {
        // 6자리 랜덤 인증번호 생성
        String verificationCode = String.format("%06d", secureRandom.nextInt(1000000));

        // Redis에 인증번호 저장
        String redisKey = EMAIL_VERIFICATION_PREFIX + email;
        Map<String, Object> verificationData = new HashMap<>();
        verificationData.put("code", verificationCode);
        verificationData.put("verified", false);

        redisTemplate.opsForHash().putAll(redisKey, verificationData);
        redisTemplate.expire(redisKey, EMAIL_VERIFICATION_TTL);

        // 이메일 발송 (내부 메서드로)
        sendEmailInternal(email, verificationCode);

        log.info("Verification code sent to email: {}", email);
    }

    // 이메일 발송 내부 메서드
    @Async
    protected void sendEmailInternal(String email, String verificationCode) {
        try {
            // 이메일 메시지 생성
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email); // 받는 사람 이메일
            message.setSubject("책모 회원가입 인증번호"); // 이메일 제목
            message.setText("인증번호: " + verificationCode + "\n\n" +
                            "인증번호는 5분간 유효합니다."); // 이메일 본문

            // 이메일 발송
            javaMailSender.send(message);
            log.info("이메일 발송 성공: {}", email);
        } catch (Exception e) {
            log.error("이메일 발송 실패: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public boolean verifyEmailCode(MemberRequestDTO.EmailVerificationRequestDTO request){
        //TODO: 이메일 인증 확인 로직 구현
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public MemberResponseDTO.SignUpResponseDTO signUp(MemberRequestDTO.SignUpRequestDTO request) {
        // TODO: 회원 가입 로직 구현
        throw new UnsupportedOperationException("추후 구현 예정");
    }

    @Override
    public void addAdditionalInfo(MemberRequestDTO.AdditionalInfoDTO request) {
        // TODO: 회원 추가 정보 입력 로직 구현
        throw new UnsupportedOperationException("추후 구현 예정");
    }
}
