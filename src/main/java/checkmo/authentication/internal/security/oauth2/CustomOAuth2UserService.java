package checkmo.authentication.internal.security.oauth2;

import checkmo.authentication.internal.converter.AuthConverter;
import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Spring Security DefaultOAuth2UserService 구현체
 * <p>
 * 소셜 로그인 시 카카오, 구글로부터 받은 사용자 정보를 처리 기존 회원 인지 신규 회원인지에 따라 다르게 처리하기 추가 정보 입력(닉네임, 관심 도서 분야 등) 필요 여부도 결정
 */

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthRepository authRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Attributes attributes = OAuth2Attributes.of(registrationId, oAuth2User.getAttributes());

        String email = attributes.getEmail();

        if (!StringUtils.hasText(email)) { // 이메일 정보가 없으면 예외
            throw new OAuth2AuthenticationException("소셜 계정에서 이메일 정보를 가져올 수 없습니다");
        }

        // 기존 회원인지 확인, 신규 회원이면 생성
        AuthUser user = authRepository.findByEmail(email)
                .orElseGet(() -> registerNewMember(attributes, registrationId));
        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }

    private AuthUser registerNewMember(OAuth2Attributes attributes, String registrationId) {
        AuthUser newUser = AuthConverter.toOAuth2User(attributes, registrationId);
        return authRepository.save(newUser);
    }
}
