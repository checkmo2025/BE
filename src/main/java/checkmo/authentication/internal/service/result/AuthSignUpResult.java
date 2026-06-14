package checkmo.authentication.internal.service.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthSignUpResult {

    private final String email;
    private final boolean profileCompleted;
}
