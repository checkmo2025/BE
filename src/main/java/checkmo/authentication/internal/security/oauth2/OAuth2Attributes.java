package checkmo.authentication.internal.security.oauth2;

import java.util.Map;
import checkmo.authentication.internal.entity.Provider;
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
            case Provider.GOOGLE -> ofGoogle(attributes);
            case Provider.KAKAO -> ofKakao(attributes);
            case Provider.NAVER -> ofNaver(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }

    private static OAuth2Attributes ofGoogle(Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .email((String) attributes.get(Provider.Google.EMAIL))
                .providerId((String) attributes.get(Provider.Google.PROVIDER_ID))
                .build();
    }

    private static OAuth2Attributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get(Provider.Kakao.ACCOUNT);
        return OAuth2Attributes.builder()
                .email((String) kakaoAccount.get(Provider.Kakao.EMAIL))
                .providerId(String.valueOf(attributes.get(Provider.Kakao.PROVIDER_ID)))
                .build();
    }

    private static OAuth2Attributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get(Provider.Naver.RESPONSE);
        return OAuth2Attributes.builder()
                .email((String) response.get(Provider.Naver.EMAIL))
                .providerId((String) response.get(Provider.Naver.PROVIDER_ID))
                .build();
    }
}
