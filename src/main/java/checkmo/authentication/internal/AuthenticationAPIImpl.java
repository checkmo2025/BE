package checkmo.authentication.internal;

import checkmo.authentication.AuthenticationAPI;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import checkmo.authentication.internal.security.jwt.AppRefreshTokenAuthenticationService;
import checkmo.authentication.internal.service.command.AuthSessionCommandService;
import checkmo.authentication.internal.service.command.AuthUserCommandService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationAPIImpl implements AuthenticationAPI {

    private final AuthUserCommandService authUserCommandService;
    private final AuthSessionCommandService authSessionCommandService;
    private final TokenCacheService tokenCacheService;
    private final AuthRepository authRepository;
    private final AppRefreshTokenAuthenticationService appRefreshTokenAuthenticationService;

    @Override
    public void deleteAuthData(Long memberId) {
        tokenCacheService.deleteRefreshToken(memberId);
        authRepository.findById(memberId).ifPresent(authRepository::delete);
    }

    @Override
    public void completeProfile(Long memberId) {
        authUserCommandService.completeProfile(memberId);
    }

    @Override
    public void deactivateMember(Long memberId, HttpServletRequest request, HttpServletResponse response) {
        authSessionCommandService.logout(request, response);
        authUserCommandService.deactivateMember(memberId);
    }

    @Override
    public boolean updatePassword(Long memberId, String currentPassword, String newPassword) {
        return authUserCommandService.updatePassword(memberId, currentPassword, newPassword);
    }

    @Override
    public void updateEmail(Long memberId, String currentEmail, String newEmail, String verificationCode) {
        authUserCommandService.updateEmail(memberId, currentEmail, newEmail, verificationCode);
    }

    @Override
    public void updateNickname(Long memberId, String nickname) {
        authUserCommandService.updateNickname(memberId, nickname);
    }

    @Override
    public boolean canAccessAdmin(Long memberId) {
        AuthUser authUser = authRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));
        return authUser.isAdmin();
    }

    @Override
    public String fetchProvider(Long memberId) {
        return authRepository.findById(memberId)
                .map(AuthUser::getProvider)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Optional<Authentication> authenticateAppRefreshToken(String refreshToken) {
        return appRefreshTokenAuthenticationService.authenticate(refreshToken);
    }
}
