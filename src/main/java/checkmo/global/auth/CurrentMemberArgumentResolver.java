package checkmo.global.auth;

import checkmo.member.entity.Member;
import checkmo.member.repository.MemberRepository;
import checkmo.member.service.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@RequiredArgsConstructor
@Component
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        // @CurrentMember Member 타입 지원
        boolean isCurrentMemberAnnotation = parameter.getParameterAnnotation(CurrentMember.class) != null;
        boolean isMemberClass = Member.class.equals(parameter.getParameterType());

        // @CurrentId String 타입 지원
        boolean isCurrentIdAnnotation = parameter.getParameterAnnotation(CurrentId.class) != null;
        boolean isStringClass = String.class.equals(parameter.getParameterType());

        return (isCurrentMemberAnnotation && isMemberClass) || (isCurrentIdAnnotation && isStringClass);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("현재 사용자는 인증되지 않은 사용자");
            return null;
        }

        // PrincipalDetails에서 memberId 추출 (Member 엔티티의 id가 memberId로 사용됨)
        String memberId = null;
        if (authentication.getPrincipal() instanceof PrincipalDetails) {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            memberId = principalDetails.getUsername();
        }

        if (memberId == null) {
            log.warn("loginId가 존재하지 않음 없음");
            return null;
        }

        // @CurrentLoginId인 경우 memberId 반환
        if (parameter.getParameterAnnotation(CurrentId.class) != null) {
            log.info("loginId 주입: {}", memberId);
            return memberId;
        }

        // @CurrentUser인 경우 User 객체 반환
        if (parameter.getParameterAnnotation(CurrentMember.class) != null) {
            Member currentUser = memberRepository.findById(memberId)
                                                 .orElse(null);

            if (currentUser != null) {
                log.info("User 객체 주입: {}", currentUser.getId());
            } else {
                log.warn("사용자를 찾을 수 없음: {}", memberId);
            }

            return currentUser;
        }

        return null;
    }
}
