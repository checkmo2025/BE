package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.exception.AuthException;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security DefaultOAuth2UserService 구현체
 * <p>
 * 소셜 로그인 시 카카오, 구글, 네이버, 애플로부터 받은 사용자 정보를 처리 기존 회원 인지 신규 회원인지에 따라 다르게 처리하기 추가 정보 입력(닉네임, 관심 도서 분야 등) 필요 여부도 결정
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAccountResolver socialAccountResolver;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, oAuth2User.getAttributes());

        SocialAccountResolution result = resolveSocialAccount(attributes, registrationId);
        return new PrincipalDetails(result.user(), oAuth2User.getAttributes(), result.newSocialSignUp());
    }

    SocialAccountResolution resolveSocialAccount(OAuth2Attributes attributes, String registrationId) {
        try {
            return socialAccountResolver.resolve(attributes, registrationId);
        } catch (AuthException e) {
            String code = e.getErrorReasonHttpStatus().getCode();
            String message = e.getErrorReasonHttpStatus().getMessage();
            throw new OAuth2AuthenticationException(new OAuth2Error(code, message, null), message, e);
        }
    }
}
