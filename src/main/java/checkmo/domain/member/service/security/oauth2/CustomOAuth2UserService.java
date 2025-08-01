package checkmo.domain.member.service.security.oauth2;

import checkmo.domain.member.converter.MemberConverter;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.repository.MemberRepository;
import checkmo.domain.member.service.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Spring Security DefaultOAuth2UserService 구현체
 *
 * 소셜 로그인 시 카카오, 구글로부터 받은 사용자 정보를 처리 기존 회원 인지 신규 회원인지에 따라 다르게 처리하기 추가 정보 입력(닉네임, 관심 도서 분야 등) 필요 여부도
 * 결정
 */

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email;

        if (registrationId.equals("google")) { // 구글 소셜 로그인 처리
            email = oAuth2User.getAttribute("email");
        } else if (registrationId.equals("kakao")) { // 카카오 소셜 로그인 처리
            // TODO: 카카오 소셜 로그인 구현 예정
            throw new OAuth2AuthenticationException("카카오 소셜 로그인은 아직 구현되지 않았습니다");
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }

        if (!StringUtils.hasText(email)) { // 이메일 정보가 없으면 예외
            throw new OAuth2AuthenticationException("소셜 계정에서 이메일 정보를 가져올 수 없습니다");
        }

        // 기존 회원인지 확인, 신규 회원이면 생성
        Member member = memberRepository.findByEmail(email)
                                        .orElseGet(() -> {
                                            String providerId = resolveProviderId(oAuth2User,
                                                registrationId);
                                            // 신규 회원인 경우, Member 객체 생성
                                            Member newMember = MemberConverter.fromOAuth2User(email,
                                                registrationId, providerId);
                                            return memberRepository.save(newMember);
                                        });
        return new PrincipalDetails(member, oAuth2User.getAttributes());
    }

    private String resolveProviderId(OAuth2User oAuth2User, String registrationId) {
        if (registrationId.equals("google")) {
            return oAuth2User.getAttribute("sub"); // Google의 경우 'sub'가 고유 ID
        } else if (registrationId.equals("kakao")) {
            // TODO: 카카오
            throw new OAuth2AuthenticationException("카카오 소셜 로그인은 아직 구현되지 않았습니다");
        }
        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }
}
