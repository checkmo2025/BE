package checkmo.authentication.internal.service.command;

import checkmo.authentication.AuthenticationEvent;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.web.dto.AuthRequestDTO;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class EmailVerificationCommandService {

    private static final String EMAIL_VERIFICATION_PREFIX = "verification:";
    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofMinutes(10); // 10분

    // 랜덤 인증번호 생성용 정적 필드
    private static final SecureRandom secureRandom = new SecureRandom();

    // 외부 서비스
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    private final AuthRepository authRepository;

    public void sendEmailVerification(String email, AuthenticationEvent.VerificationType type) {
        // 이미 인증번호가 Redis에 존재하면 예외 처리
        String redisKey = EMAIL_VERIFICATION_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new AuthException(AuthErrorStatus.EMAIL_VERIFICATION_CODE_ALREADY_SENT);
        }

        // 이미 회원가입이 완료된 이메일인지 확인하는 로직
        if (authRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        // 6자리 랜덤 인증번호 생성
        String verificationCode = String.format("%06d", secureRandom.nextInt(1000000));

        // Redis에 인증번호 저장
        Map<String, Object> verificationData = new HashMap<>();
        verificationData.put("code", verificationCode);
        verificationData.put("verified", false);

        redisTemplate.opsForHash().putAll(redisKey, verificationData);
        redisTemplate.expire(redisKey, EMAIL_VERIFICATION_TTL);

        eventPublisher.publishEvent(
                AuthenticationEvent.SendVerificationEmail.builder()
                        .email(email)
                        .verificationCode(verificationCode)
                        .type(type)
                        .build());
    }

    public boolean verifyEmailCode(AuthRequestDTO.EmailVerification request) {
        String redisKey = EMAIL_VERIFICATION_PREFIX + request.getEmail();

        // redis에서 인증 정보 조회
        String storedCode = (String) redisTemplate.opsForHash().get(redisKey, "code");
        Boolean isVerified = (Boolean) redisTemplate.opsForHash().get(redisKey, "verified");

        // 인증번호가 만료된 경우
        if (storedCode == null) {
            throw new AuthException(AuthErrorStatus.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        // 인증번호가 일치하지 않는 경우
        if (!request.getVerificationCode().equals(storedCode)) {
            throw new AuthException(AuthErrorStatus.EMAIL_VERIFICATION_CODE_INVALID);
        }

        // 이미 인증된 경우
        if (Boolean.TRUE.equals(isVerified)) {
            throw new AuthException(AuthErrorStatus.EMAIL_VERIFICATION_CODE_ALREADY_VERIFIED);
        }

        // 인증 성공 시 verified 상태 업데이트
        redisTemplate.opsForHash().put(redisKey, "verified", true);

        return true;
    }

    public void sendTempPassword(String email) {
        AuthUser user = authRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        String tempPassword = generateTempPassword();

        user.updatePassword(passwordEncoder.encode(tempPassword));

        eventPublisher.publishEvent(
            AuthenticationEvent.SendTempPassword.builder()
                                                     .email(email)
                                                     .tempPassword(tempPassword)
                                                     .build());
    }

    private String generateTempPassword() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specials = "!@#$%^&*";
        String all = letters + digits + specials;

        List<Character> chars = new ArrayList<>();

        chars.add(letters.charAt(secureRandom.nextInt(letters.length())));   // 문자 1개
        chars.add(digits.charAt(secureRandom.nextInt(digits.length())));     // 숫자 1개
        chars.add(specials.charAt(secureRandom.nextInt(specials.length()))); // 특수 1개

        for (int i = 0; i < 9; i++) {
            chars.add(all.charAt(secureRandom.nextInt(all.length())));
        }

        Collections.shuffle(chars, secureRandom);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }

        return sb.toString();
    }
}
