package checkmo.authentication.internal.service.result;

import checkmo.authentication.internal.security.jwt.JwtToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

@Getter
@RequiredArgsConstructor
public class AuthTokenRotationResult {

    private final Long memberId;
    private final Authentication authentication;
    private final JwtToken jwtToken;
}
