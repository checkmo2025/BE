package checkmo.authentication.internal.converter;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.security.oauth2.OAuth2Attributes;
import checkmo.authentication.web.dto.AuthRequestDTO;
import checkmo.authentication.web.dto.AuthResponseDTO;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConverter {

    public static AuthResponseDTO.SignUp toSignUpDTO(AuthUser user) {
        return AuthResponseDTO.SignUp.builder()
                .email(user.getEmail())
                .isProfileCompleted(user.isProfileCompleted())
                .build();
    }

    public static AuthUser toOAuth2User(OAuth2Attributes attributes, String registrationId) {
        String newMemberId = registrationId.toUpperCase() + "_" + attributes.getProviderId();

        return AuthUser.builder()
                .id(newMemberId)
                .email(attributes.getEmail())
                .password("")
                .role(Role.USER)
                .deactivatedAt(null)
                .profileCompleted(false)
                .build();
    }

    public static AuthUser toLocalUser(AuthRequestDTO.SignUp request, String encodedPassword) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String newUserId = "LOCAL_" + uuid;

        return AuthUser.builder()
                .id(newUserId)
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Role.USER)
                .deactivatedAt(null)
                .profileCompleted(false)
                .build();
    }
}
