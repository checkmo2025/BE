package checkmo.authentication.internal.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtToken {
    private String sessionId;
    private String accessToken;
    private String refreshToken;
}
