package checkmo.authentication.internal.security.apple;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleJwks(List<AppleJwk> keys) {
}
