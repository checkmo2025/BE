package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

class CustomOAuth2UserServiceTest {

    @Test
    void translatesAuthExceptionToOAuth2AuthenticationException() {
        SocialAccountResolver socialAccountResolver = mock(SocialAccountResolver.class);
        CustomOAuth2UserService service = new CustomOAuth2UserService(socialAccountResolver);
        OAuth2Attributes attributes = OAuth2Attributes.of("google", Map.of(
                "email", "social-user@example.com",
                "sub", "google-sub"
        ));
        when(socialAccountResolver.resolve(attributes, "google"))
                .thenThrow(new AuthException(AuthErrorStatus.SOCIAL_ACCOUNT_EMAIL_CONFLICT));

        Throwable thrown = catchThrowable(() -> service.resolveSocialAccount(attributes, "google"));

        assertSoftly(softly -> {
            softly.assertThat(thrown).isInstanceOf(OAuth2AuthenticationException.class);
            OAuth2AuthenticationException exception = (OAuth2AuthenticationException) thrown;
            softly.assertThat(exception.getError().getErrorCode()).isEqualTo("AUTH_414");
            softly.assertThat(exception).hasCauseInstanceOf(AuthException.class);
        });
    }
}
