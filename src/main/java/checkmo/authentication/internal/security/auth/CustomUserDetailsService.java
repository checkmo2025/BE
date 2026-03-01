package checkmo.authentication.internal.security.auth;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService 구현체
 * <p>
 * 일반 로그인 시 id로 회원을 조회하여 UserDetails 객체로 변환 비활성화된 계정, 탈퇴한 계정 등의 상태 검증도 여기서 처리 loadUserByUsername 메소드 단순하게 오버라이딩 하면 됨
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthUser user = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

        // 로그인 성공 시점 자동 복구 정책 때문에 로그인 단계에서는 비활성 상태를 차단하지 않는다.
        return new PrincipalDetails(user);
    }

    public UserDetails loadUserById(String id) {
        AuthUser user = authRepository.findById(id)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));

        validateMemberStatus(user);
        return new PrincipalDetails(user);
    }

    private void validateMemberStatus(AuthUser user) {
        if (user.getDeactivatedAt() != null) {
            throw new AuthException(AuthErrorStatus.MEMBER_INACTIVE);
        }
    }
}
