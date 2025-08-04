package checkmo.domain.member.service.security.oauth2;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OAuth2Attributes {

    private final String email;
    private final String providerId;

    public static OAuth2Attributes of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }

    private static OAuth2Attributes ofGoogle(Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .email((String) attributes.get("email"))
                .providerId((String) attributes.get("sub"))
                .build();
    }

    private static OAuth2Attributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return OAuth2Attributes.builder()
                .email((String) kakaoAccount.get("email"))
                .providerId(String.valueOf(attributes.get("id")))
                .build();
    }
}
