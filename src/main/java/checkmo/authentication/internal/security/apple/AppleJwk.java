package checkmo.authentication.internal.security.apple;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleJwk(
        String kid,
        String kty,
        String use,
        String alg,
        String n,
        String e
) {
}
