package checkmo.authentication.internal;

import checkmo.authentication.AuthenticationAPI;
import checkmo.authentication.internal.service.command.AuthUserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationAPIImpl implements AuthenticationAPI {

    private final AuthUserCommandService authUserCommandService;

    @Override
    public void completeProfile(String memberId) {
        authUserCommandService.completeProfile(memberId);
    }
}
