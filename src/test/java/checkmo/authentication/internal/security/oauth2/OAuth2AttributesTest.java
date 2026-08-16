package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

class OAuth2AttributesTest {

    @Test
    void mapsGoogleKakaoNaver() {
        OAuth2Attributes google = OAuth2Attributes.of("google", Map.of(
                "email", "google-user@example.com",
                "sub", "google-sub"
        ));

        OAuth2Attributes kakao = OAuth2Attributes.of("kakao", Map.of(
                "id", 12345L,
                "kakao_account", Map.of("email", "kakao-user@example.com")
        ));

        OAuth2Attributes naver = OAuth2Attributes.of("naver", Map.of(
                "response", Map.of(
                        "email", "naver-user@example.com",
                        "id", "naver-id"
                )
        ));

        assertSoftly(softly -> {
            softly.assertThat(google.getEmail()).isEqualTo("google-user@example.com");
            softly.assertThat(google.getProviderId()).isEqualTo("google-sub");
            softly.assertThat(kakao.getEmail()).isEqualTo("kakao-user@example.com");
            softly.assertThat(kakao.getProviderId()).isEqualTo("12345");
            softly.assertThat(naver.getEmail()).isEqualTo("naver-user@example.com");
            softly.assertThat(naver.getProviderId()).isEqualTo("naver-id");
        });
    }

    @Test
    void mapsApple() {
        OAuth2Attributes apple = OAuth2Attributes.of("apple", Map.of(
                "email", "apple-user@example.com",
                "sub", "apple-sub"
        ));

        assertSoftly(softly -> {
            softly.assertThat(apple.getEmail()).isEqualTo("apple-user@example.com");
            softly.assertThat(apple.getProviderId()).isEqualTo("apple-sub");
        });
    }

    @Test
    void rejectsAppleWithoutSubject() {
        assertThatThrownBy(() -> OAuth2Attributes.of("apple", Map.of("email", "apple-user@example.com")))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }
}
