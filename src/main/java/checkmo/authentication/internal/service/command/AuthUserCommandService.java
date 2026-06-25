package checkmo.authentication.internal.service.command;

import checkmo.authentication.AuthenticationEvent;
import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.web.dto.AuthRequestDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthUserCommandService {

    private static final String EMAIL_VERIFICATION_PREFIX = "verification:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationCommandService emailVerificationCommandService;

    private final ApplicationEventPublisher eventPublisher;

    public AuthUser signUp(AuthRequestDTO.SignUp request) {
        // 이메일 중복 확인
        authRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (existing.getNickName() == null) {
                throw new AuthException(AuthErrorStatus.SIGNUP_INCOMPLETE);
            }
            throw new AuthException(AuthErrorStatus.MEMBER_ALREADY_EXISTS);
        });

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

        eventPublisher.publishEvent(
                AuthenticationEvent.CreateMember.builder()
                        .id(savedUser.getId())
                        .email(savedUser.getEmail())
                        .agreements(toTermsAgreements(request))
                        .build());

        deleteEmailVerificationAfterCommit(redisKey);

        return newUser;
    }

    private List<AuthenticationEvent.TermsAgreement> toTermsAgreements(AuthRequestDTO.SignUp request) {
        if (request.getAgreements() == null) {
            return List.of();
        }

        return request.getAgreements()
                .stream()
                .map(agreement -> new AuthenticationEvent.TermsAgreement(
                        agreement.getTermsId(),
                        agreement.getAgreed()
                ))
                .toList();
    }

    private void deleteEmailVerificationAfterCommit(String redisKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            redisTemplate.delete(redisKey);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.delete(redisKey);
            }
        });
    }

    public void completeProfile(String userId) {
        AuthUser authUser = authRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        if (authUser.isProfileCompleted()) {
            return;
        }

        authUser.completeProfile();
    }

    public void deactivateMember(String memberId) {
        AuthUser authUser = authRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        if (authUser.isDeactivated()) {
            return;
        }

        authUser.deactivate();
    }

    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        AuthUser user = authRepository.findById(userId)
                                      .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AuthException(AuthErrorStatus.PASSWORD_SAME_AS_OLD);
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
        return true;
    }

    public void updateEmail(String memberId, String currentEmail, String newEmail, String verificationCode) {
        AuthUser authUser = authRepository.findById(memberId)
                                          .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        // 소셜 유저 차단
        if (!authUser.getId().startsWith("LOCAL_")) {
            throw new AuthException(AuthErrorStatus.SOCIAL_MEMBER_CANNOT_CHANGE_EMAIL);
        }

        // 입력한 기존 이메일이 실제 DB 값과 일치하는지
        if (!authUser.getEmail().equals(currentEmail)) {
            throw new AuthException(AuthErrorStatus.CURRENT_EMAIL_INCORRECT);
        }

        // 인증번호 검증
        emailVerificationCommandService.verifyEmailCode(new AuthRequestDTO.EmailVerification(newEmail, verificationCode));

        authUser.updateEmail(newEmail);
    }

    public void updateNickname(String memberId, String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new AuthException(AuthErrorStatus.NICKNAME_REQUIRED);
        }

        AuthUser authUser = authRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        authUser.updateNickname(nickname);
    }
}
