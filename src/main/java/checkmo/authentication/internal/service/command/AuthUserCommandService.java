package checkmo.authentication.internal.service.command;

import checkmo.authentication.AuthenticationEvent;
import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.web.dto.AuthRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthUserCommandService {

    private static final String EMAIL_VERIFICATION_PREFIX = "verification:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher eventPublisher;

    public AuthUser signUp(AuthRequestDTO.SignUp request) {
        // TODO: Member 모듈의 API를 통해 필수 약관 동의 여부 체크
        
        // 이메일 중복 확인
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(AuthErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        // 이메일 인증 여부 확인
        String redisKey = EMAIL_VERIFICATION_PREFIX + request.getEmail();
        Boolean isVerified = (Boolean) redisTemplate.opsForHash().get(redisKey, "verified");
        if (!Boolean.TRUE.equals(isVerified)) {
            throw new AuthException(AuthErrorStatus.EMAIL_NOT_VERIFIED);
        }

        // 회원 정보 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        AuthUser newUser = AuthConverter.toLocalUser(request, encodedPassword);

        AuthUser savedUser = authRepository.save(newUser);
        redisTemplate.delete(redisKey); // 회원가입 후 인증 정보 삭제

        eventPublisher.publishEvent(
                AuthenticationEvent.CreateMember.builder()
                        .id(savedUser.getId())
                        .email(savedUser.getEmail())
                        // TODO: 동의한 약관 ID 리스트 보냄
                        .build());

        return newUser;
    }

    public void completeProfile(String userId) {
        AuthUser authUser = authRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        if (authUser.isProfileCompleted()) {
            return;
        }

        authUser.completeProfile();
    }

    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        AuthUser user = authRepository.findById(userId)
                                      .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
        return true;
    }
}
