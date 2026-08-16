package checkmo.authentication.internal.security.oauth2;

import java.util.Map;
import checkmo.authentication.internal.entity.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.util.StringUtils;

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
            case Provider.APPLE -> ofApple(attributes);
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
        String providerId = kakaoProviderId(attributes.get(Provider.Kakao.PROVIDER_ID));
        Map<?, ?> kakaoAccount = kakaoAccount(attributes.get(Provider.Kakao.ACCOUNT));
        String email = stringValue(kakaoAccount.get(Provider.Kakao.EMAIL));

        if (!StringUtils.hasText(email)) {
            boolean emailNeedsAgreement = Boolean.TRUE.equals(kakaoAccount.get("email_needs_agreement"));
            String errorCode = emailNeedsAgreement
                    ? OAuth2ErrorCodes.KAKAO_EMAIL_CONSENT_REQUIRED
                    : OAuth2ErrorCodes.KAKAO_EMAIL_UNAVAILABLE;
            throw kakaoAuthenticationException(errorCode, "카카오 계정 이메일을 사용할 수 없습니다");
        }

        return OAuth2Attributes.builder()
                .email(email)
                .providerId(providerId)
                .build();
    }

    private static String kakaoProviderId(Object value) {
        if (value instanceof Number number && number.longValue() > 0) {
            return String.valueOf(number.longValue());
        }
        if (value instanceof String string && StringUtils.hasText(string)) {
            return string;
        }
        throw kakaoAuthenticationException(
                OAuth2ErrorCodes.INVALID_KAKAO_USER_INFO,
                "카카오 사용자 식별자를 가져올 수 없습니다"
        );
    }

    private static Map<?, ?> kakaoAccount(Object value) {
        if (value instanceof Map<?, ?> account) {
            return account;
        }
        throw kakaoAuthenticationException(
                OAuth2ErrorCodes.INVALID_KAKAO_USER_INFO,
                "카카오 계정 정보를 가져올 수 없습니다"
        );
    }

    private static String stringValue(Object value) {
        return value instanceof String string ? string : null;
    }

    private static OAuth2AuthenticationException kakaoAuthenticationException(
            String errorCode,
            String description
    ) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(errorCode, description, null),
                description
        );
    }

    private static OAuth2Attributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get(Provider.Naver.RESPONSE);
        return OAuth2Attributes.builder()
                .email((String) response.get(Provider.Naver.EMAIL))
                .providerId((String) response.get(Provider.Naver.PROVIDER_ID))
                .build();
    }

    private static OAuth2Attributes ofApple(Map<String, Object> attributes) {
        String providerId = (String) attributes.get(Provider.Apple.PROVIDER_ID);
        if (!StringUtils.hasText(providerId)) {
            throw new OAuth2AuthenticationException("Apple 계정 식별자를 가져올 수 없습니다");
        }
        return OAuth2Attributes.builder()
                .email((String) attributes.get(Provider.Apple.EMAIL))
                .providerId(providerId)
                .build();
    }
}
