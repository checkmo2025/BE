package checkmo.domain.member.service.authenticate;

import checkmo.apiPayload.code.status.ErrorStatus;
import checkmo.apiPayload.exception.GeneralException;
import checkmo.domain.member.service.security.auth.PrincipalDetails;
import checkmo.domain.member.service.security.jwt.JwtCookieUtil;
import checkmo.domain.member.service.security.jwt.JwtToken;
import checkmo.domain.member.service.security.jwt.JwtTokenProvider;
import checkmo.domain.member.service.security.jwt.TokenCacheService;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAuthenticationServiceImpl implements MemberAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenCacheService tokenCacheService;
    private final JwtCookieUtil jwtCookieUtil;

    @Override
    public void login(MemberRequestDTO.LoginRequestDTO request, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        try {
            // 인증 요청
            Authentication authentication =
                authenticationManager.authenticate(authenticationToken);

            /// 인증 성공 후 SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 토큰 생성
            JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

            jwtCookieUtil.addTokenToCookie(response, "accessToken", jwtToken.getAccessToken(), 2 * 60 * 60); // 2시간 유효
            jwtCookieUtil.addTokenToCookie(response, "refreshToken", jwtToken.getRefreshToken(), 14 * 24 * 60 * 60); // 14일 유효

            // RefreshToken Redis에 저장
            String memberId = ((PrincipalDetails) authentication.getPrincipal()).getMember().getId();
            tokenCacheService.saveRefreshToken(memberId, jwtToken.getRefreshToken());

        } catch (Exception e) {
            // 인증 실패 시 예외 처리
            throw new GeneralException(ErrorStatus.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 일치하지 않습니다");
        }
    }

    @Override
    public void logout(String token) {
        // TODO: 로그아웃 로직 구현
    }

    @Override
    public void reactivateMember() {
        // TODO: 계정 복구 로직 구현
    }
}
