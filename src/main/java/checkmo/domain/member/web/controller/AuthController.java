package checkmo.domain.member.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃, 이메일 인증, 소셜 로그인 관련 API")
public class AuthController {

    // 이메일 인증 관련
    // POST /api/auth/email-verification - 이메일 인증 요청
    // POST /api/auth/email-verification/confirm - 이메일 인증 확인

    // 회원가입 관련
    // POST /api/auth/signup - 회원가입
    // POST /api/auth/additional-info - 회원 추가 정보 입력

    // 소셜 로그인 관련
    // GET /api/auth/oauth2/google - 소셜 로그인 (구글)
    // GET /api/auth/oauth2/kakao - 소셜 로그인 (카카오)

    // 일반 로그인/로그아웃 관련
    // POST /api/auth/login - 이메일 로그인
    // POST /api/auth/logout - 로그아웃
}
