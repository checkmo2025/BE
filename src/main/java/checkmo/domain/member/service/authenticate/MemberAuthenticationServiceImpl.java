package checkmo.domain.member.service.authenticate;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.entity.Member;
import checkmo.domain.member.service.security.auth.PrincipalDetails;
import checkmo.domain.member.service.security.jwt.JwtCookieUtil;
import checkmo.domain.member.service.security.jwt.JwtLoginProcessor;
import checkmo.domain.member.service.security.jwt.JwtTokenProvider;
import checkmo.domain.member.service.security.jwt.TokenCacheService;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberAuthenticationServiceImpl implements MemberAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCacheService tokenCacheService;
    private final JwtCookieUtil jwtCookieUtil;
    private final JwtLoginProcessor jwtLoginProcessor;

    @Override
    public Member login(MemberRequestDTO.LoginRequestDTO request, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication;

        try {
            // 인증 요청
            authentication = authenticationManager.authenticate(authenticationToken);

            /// 인증 성공 후 SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 토큰 생성 및 쿠키 설정
            jwtLoginProcessor.processLogin(response, authentication);

        } catch (AuthenticationException authEx) {
            // 인증 실패 시 예외 처리
            throw new GeneralException(ErrorStatus.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 일치하지 않습니다");
        } catch (Exception e) {
            // 기타 예외 처리
            throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류: 로그인 처리 중 오류가 발생했습니다");
        }

        // 인증 성공 후 멤버 객체 반환
        return ((PrincipalDetails) authentication.getPrincipal()).getMember();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        // 0. 쿠키에서 jwt 토큰 가져오기
        String accessToken = jwtCookieUtil.resolveToken(request, "accessToken");
        String refreshToken = jwtCookieUtil.resolveToken(request, "refreshToken");

        // 1. jwt 토큰을 쿠키에서 삭제
        jwtCookieUtil.deleteTokenFromCookie(response, "accessToken");
        jwtCookieUtil.deleteTokenFromCookie(response, "refreshToken");

        // 2. redis에 저장된 Access Token을 블랙리스트에 추가하여 무효화
        if (StringUtils.hasText(accessToken)) {
            try {
                tokenCacheService.saveBlacklistToken(accessToken);
            } catch (Exception e) {
                log.error("[로그아웃] AccessToken 블랙리스트 저장 실패", e);
            }
        }

        // 3. redis에 저장된 Refresh Token을 redis에서 삭제
        if (StringUtils.hasText(refreshToken)) {
            try{
                String memberId = jwtTokenProvider.getUserIdFromToken(refreshToken);
                tokenCacheService.deleteRefreshToken(memberId);
            } catch (Exception e) {
                log.error("[로그아웃] RefreshToken 삭제 실패", e);
            }
        }
    }

    @Override
    public void reactivateMember() {
        // TODO: 계정 복구 로직 구현
    }
}
