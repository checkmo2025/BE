package checkmo.authentication.internal.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.exception.AuthErrorStatus;
import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.repository.AuthRepository;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SocialAccountResolverTest {

    private final AuthRepository authRepository = mock(AuthRepository.class);
    private final SocialAccountCreator socialAccountCreator = mock(SocialAccountCreator.class);
    private final SocialAccountResolver resolver = new SocialAccountResolver(authRepository, socialAccountCreator);

    @Test
    void createsIncompleteAppleUserWhenFirstLoginHasEmail() {
        OAuth2Attributes attributes = appleAttributes("apple-sub", "apple-user@example.com");

        when(authRepository.findById("APPLE_apple-sub")).thenReturn(Optional.empty());
        when(authRepository.findByEmail("apple-user@example.com")).thenReturn(Optional.empty());
        when(socialAccountCreator.create(attributes, "apple"))
                .thenReturn(user("APPLE_apple-sub", "apple-user@example.com"));

        SocialAccountResolution result = resolver.resolve(attributes, "apple");

        assertSoftly(softly -> {
            softly.assertThat(result.newSocialSignUp()).isTrue();
            softly.assertThat(result.user().getId()).isEqualTo("APPLE_apple-sub");
            softly.assertThat(result.user().getEmail()).isEqualTo("apple-user@example.com");
            softly.assertThat(result.user().isProfileCompleted()).isFalse();
        });
        verify(socialAccountCreator).create(attributes, "apple");
    }

    @Test
    void resolvesRepeatAppleLoginByProviderIdWithoutEmail() {
        AuthUser existingUser = user("APPLE_apple-sub", "apple-user@example.com");
        OAuth2Attributes attributes = appleAttributesWithoutEmail("apple-sub");

        when(authRepository.findById("APPLE_apple-sub")).thenReturn(Optional.of(existingUser));

        SocialAccountResolution result = resolver.resolve(attributes, "apple");

        assertSoftly(softly -> {
            softly.assertThat(result.user()).isSameAs(existingUser);
            softly.assertThat(result.newSocialSignUp()).isFalse();
        });
        verify(authRepository, never()).findByEmail(any());
        verify(socialAccountCreator, never()).create(attributes, "apple");
    }

    @Test
    void rejectsAppleEmailCollisionWithControlledConflict() {
        OAuth2Attributes attributes = appleAttributes("apple-sub", "shared@example.com");

        when(authRepository.findById("APPLE_apple-sub")).thenReturn(Optional.empty());
        when(authRepository.findByEmail("shared@example.com"))
                .thenReturn(Optional.of(user("LOCAL_local-user", "shared@example.com")));

        Throwable thrown = catchThrowable(() -> resolver.resolve(attributes, "apple"));

        assertAuthException(thrown, AuthErrorStatus.SOCIAL_ACCOUNT_EMAIL_CONFLICT, HttpStatus.CONFLICT);
        verify(socialAccountCreator, never()).create(attributes, "apple");
    }

    @Test
    void rejectsNewAppleLoginWithoutEmail() {
        OAuth2Attributes attributes = appleAttributesWithoutEmail("apple-sub");

        when(authRepository.findById("APPLE_apple-sub")).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> resolver.resolve(attributes, "apple"));

        assertAuthException(thrown, AuthErrorStatus.APPLE_EMAIL_REQUIRED, HttpStatus.BAD_REQUEST);
        verify(socialAccountCreator, never()).create(attributes, "apple");
    }

    @Test
    void resolvesConcurrentAppleFirstLoginWhenDuplicateSaveCanRefetchById() {
        AuthUser existingUser = user("APPLE_apple-sub", "apple-user@example.com");
        OAuth2Attributes attributes = appleAttributes("apple-sub", "apple-user@example.com");

        when(authRepository.findById("APPLE_apple-sub"))
                .thenReturn(Optional.empty(), Optional.of(existingUser));
        when(authRepository.findByEmail("apple-user@example.com")).thenReturn(Optional.empty());
        when(socialAccountCreator.create(attributes, "apple"))
                .thenThrow(new DataIntegrityViolationException("duplicate auth_user"));

        SocialAccountResolution result = resolver.resolve(attributes, "apple");

        assertSoftly(softly -> {
            softly.assertThat(result.user()).isSameAs(existingUser);
            softly.assertThat(result.newSocialSignUp()).isFalse();
        });
    }

    @ParameterizedTest
    @MethodSource("emailBasedSocialAttributes")
    void resolvesEmailBasedSocialLoginByEmailEvenWhenProviderIdDiffers(
            String registrationId,
            String newProviderMemberId,
            OAuth2Attributes attributes
    ) {
        AuthUser existingUser = user(registrationId.toUpperCase() + "_old-provider-id", "social-user@example.com");

        when(authRepository.findByEmail("social-user@example.com")).thenReturn(Optional.of(existingUser));

        SocialAccountResolution result = resolver.resolve(attributes, registrationId);

        assertSoftly(softly -> {
            softly.assertThat(result.user()).isSameAs(existingUser);
            softly.assertThat(result.newSocialSignUp()).isFalse();
        });
        verify(authRepository, never()).findById(newProviderMemberId);
        verify(socialAccountCreator, never()).create(attributes, registrationId);
    }

    private OAuth2Attributes appleAttributes(String subject, String email) {
        return OAuth2Attributes.of("apple", Map.of(
                "sub", subject,
                "email", email
        ));
    }

    private OAuth2Attributes appleAttributesWithoutEmail(String subject) {
        return OAuth2Attributes.of("apple", Map.of("sub", subject));
    }

    private AuthUser user(String id, String email) {
        return AuthUser.builder()
                .id(id)
                .email(email)
                .password("")
                .role(Role.USER)
                .profileCompleted(false)
                .build();
    }

    private static Stream<Arguments> emailBasedSocialAttributes() {
        return Stream.of(
                Arguments.of("google", "GOOGLE_new-provider-id", OAuth2Attributes.of("google", Map.of(
                        "email", "social-user@example.com",
                        "sub", "new-provider-id"
                ))),
                Arguments.of("kakao", "KAKAO_12345", OAuth2Attributes.of("kakao", Map.of(
                        "id", 12345L,
                        "kakao_account", Map.of("email", "social-user@example.com")
                ))),
                Arguments.of("naver", "NAVER_naver-new-id", OAuth2Attributes.of("naver", Map.of(
                        "response", Map.of(
                                "email", "social-user@example.com",
                                "id", "naver-new-id"
                        )
                )))
        );
    }

    private void assertAuthException(Throwable thrown, AuthErrorStatus status, HttpStatus httpStatus) {
        assertThat(thrown).isInstanceOf(AuthException.class);
        AuthException authException = (AuthException) thrown;
        assertSoftly(softly -> {
            softly.assertThat(authException.getErrorCode()).isEqualTo(status);
            softly.assertThat(authException.getErrorReasonHttpStatus().getHttpStatus()).isEqualTo(httpStatus);
        });
    }
}
