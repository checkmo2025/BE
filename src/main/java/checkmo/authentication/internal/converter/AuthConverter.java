package checkmo.authentication.internal.converter;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.security.oauth2.OAuth2Attributes;
import checkmo.authentication.web.dto.AuthRequestDTO;
import checkmo.authentication.web.dto.AuthResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConverter {

    public static AuthResponseDTO.SignUp fromUserToSignUp(AuthUser user) {
        return AuthResponseDTO.SignUp.builder()
                .email(user.getEmail())
                .isProfileCompleted(user.isProfileCompleted())
                .build();
    }

    public static AuthResponseDTO.Login fromNicknameToLogin(String nickname) {
        return AuthResponseDTO.Login.builder()
                .nickname(nickname)
                .build();
    }

    public static AuthUser fromOAuth2Attributes(OAuth2Attributes attributes, String registrationId) {
        String newMemberId = registrationId.toUpperCase() + "_" + attributes.getProviderId();

        return AuthUser.builder()
                .id(newMemberId)
                .email(attributes.getEmail())
                .password("") // OAuth2 사용자는 비밀번호가 없음
                .role(Role.USER) // 기본 역할 설정
                .deactivatedAt(null)
                .profileCompleted(false) // 프로필 미완료 상태로 설정
                .build();
    }

    public static AuthUser fromSignUpToAuthUser(
            AuthRequestDTO.SignUp request,
            String encodedPassword
    ) {

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
