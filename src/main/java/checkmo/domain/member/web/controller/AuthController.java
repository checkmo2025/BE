package checkmo.domain.member.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.member.facade.MemberCommandFacade;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃, 이메일 인증, 소셜 로그인 관련 API")
public class AuthController {

    private final MemberCommandFacade memberCommandFacade;

    // 이메일 인증 요청
    @Operation(summary = "이메일 인증번호 요청", description = "회원가입 시 이메일 인증번호를 요청합니다.")
    @PostMapping("/email-verification")
    public ApiResponse<String> sendEmailVerification(@RequestParam
                                                         @Email(message = "유효한 이메일 주소를 입력해주세요")
                                                         @NotBlank(message = "이메일은 필수입니다")
                                                         String email) {
        memberCommandFacade.sendEmailVerification(email);
        return ApiResponse.onSuccess("인증번호가 이메일로 발송되었습니다.");
    }

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
