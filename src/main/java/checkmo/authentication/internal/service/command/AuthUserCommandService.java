package checkmo.authentication.internal.service.command;

import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.web.dto.AuthRequestDTO;
import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.MemberAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthUserCommandService {

    private static final String EMAIL_VERIFICATION_PREFIX = "verification:";

    private final MemberAPI memberAPI;

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthUser signUp(AuthRequestDTO.SignUp request) {

        // 이메일 중복 확인
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_EXISTS);
        }

        // 이메일 인증 여부 확인
        String redisKey = EMAIL_VERIFICATION_PREFIX + request.getEmail();
        Boolean isVerified = (Boolean) redisTemplate.opsForHash().get(redisKey, "verified");
        if (!Boolean.TRUE.equals(isVerified)) {
            throw new GeneralException(ErrorStatus.EMAIL_NOT_VERIFIED);
        }

        // 회원 정보 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        AuthUser newUser = AuthConverter.fromSignUpToAuthUser(request, encodedPassword);

        AuthUser savedUser = authRepository.save(newUser);
        redisTemplate.delete(redisKey); // 회원가입 후 인증 정보 삭제

        memberAPI.createInitialMember(savedUser.getId(), savedUser.getEmail());

        return newUser;
    }

    public void completeProfile(String userId) {
        AuthUser authUser = authRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (authUser.isProfileCompleted()) {
            return;
        }

        authUser.completeProfile();
    }
}
