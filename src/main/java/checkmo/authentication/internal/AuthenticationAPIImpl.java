package checkmo.authentication.internal;

import checkmo.authentication.AuthenticationAPI;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import checkmo.authentication.internal.service.command.AuthUserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationAPIImpl implements AuthenticationAPI {

    private final AuthUserCommandService authUserCommandService;
    private final TokenCacheService tokenCacheService;
    private final AuthRepository authRepository;

    @Override
    public void deleteAuthData(String memberId) {
        tokenCacheService.deleteRefreshToken(memberId);
        authRepository.findById(memberId).ifPresent(authRepository::delete);
    }

    @Override
    public void completeProfile(String memberId) {
        authUserCommandService.completeProfile(memberId);
    }
}
