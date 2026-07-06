package checkmo.authentication.internal.resolver;

import checkmo.authentication.CurrentId;
import checkmo.authentication.internal.security.auth.PrincipalDetails;
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
@Component
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isCurrentIdAnnotation = parameter.getParameterAnnotation(CurrentId.class) != null;
        boolean isStringClass = String.class.equals(parameter.getParameterType());
        boolean isLongClass = Long.class.equals(parameter.getParameterType()) || long.class.equals(parameter.getParameterType());

        return isCurrentIdAnnotation && (isStringClass || isLongClass);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("현재 사용자는 인증되지 않은 사용자");
            return null;
        }

        Long memberId = null;
        if (authentication.getPrincipal() instanceof PrincipalDetails principalDetails) {
            memberId = principalDetails.getUser().getId();
        }

        if (memberId == null) {
            log.warn("loginId가 존재하지 않음 없음");
            return null;
        }

        if (parameter.getParameterAnnotation(CurrentId.class) != null) {
            log.info("loginId 주입: {}", memberId);
            if (String.class.equals(parameter.getParameterType())) {
                return String.valueOf(memberId);
            }
            return memberId;
        }

        return null;
    }
}
