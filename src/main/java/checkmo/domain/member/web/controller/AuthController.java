package checkmo.domain.member.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.member.facade.MemberCommandFacade;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃, 이메일 인증, 소셜 로그인 관련 API")
public class AuthController {

    private final MemberCommandFacade memberCommandFacade;
    private final MemberQueryFacade memberQueryFacade;

    // 이메일 인증 요청
    @Operation(summary = "이메일 인증번호 요청", description = "회원가입 시 이메일 인증번호를 요청합니다.")
    @Parameter(name = "email", description = "인증을 요청할 이메일 주소", required = true, example = "test@example.com")
    @PostMapping("/email-verification")
    public ApiResponse<String> sendEmailVerification(@RequestParam
                                                     @Email(message = "유효한 이메일 주소를 입력해주세요")
                                                     @NotBlank(message = "이메일은 필수입니다")
                                                     String email) {
        memberCommandFacade.sendEmailVerification(email);
        return ApiResponse.onSuccess("인증번호가 이메일로 발송되었습니다.");
    }

    // 이메일 인증 확인
    @Operation(summary = "이메일 인증번호 확인", description = "이메일 인증번호를 확인합니다.")
    @PostMapping("/email-verification/confirm")
    public ApiResponse<Boolean> verifyEmailCode(
        @Valid @RequestBody MemberRequestDTO.EmailVerificationRequestDTO request) {
        boolean isVerified = memberCommandFacade.verifyEmailCode(request);
        return ApiResponse.onSuccess(isVerified);
    }

    // 회원가입
    @Operation(summary = "회원가입", description = "이메일 인증 완료 후 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ApiResponse<MemberResponseDTO.SignUpResponseDTO> signUp(
        @Valid @RequestBody MemberRequestDTO.SignUpRequestDTO request,
        HttpServletResponse response) {
        return ApiResponse.onSuccess(memberCommandFacade.signUp(request, response));
    }

    // 회원 추가 정보 입력
    @Operation(summary = "회원 추가 정보 입력", description = "회원가입 후 추가 정보를 입력합니다.")
    @PostMapping("/additional-info")
    public ApiResponse<Void> addAdditionalInfo(
        @Valid @RequestBody MemberRequestDTO.AdditionalInfoDTO request) {
        memberCommandFacade.addAdditionalInfo(request);
        return ApiResponse.onSuccess(null);
    }

    // 닉네임 중복 확인
    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임 중복을 확인합니다.")
    @PostMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam
                                              @NotBlank(message = "닉네임은 필수입니다")
                                              @Size(max = 6, message = "닉네임은 최대 6자까지 가능합니다") String nickname) {
        boolean isDuplicated = memberQueryFacade.isNicknameDuplicated(nickname);
        return ApiResponse.onSuccess(isDuplicated);
    }

    // 이메일 로그인
    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ApiResponse<MemberResponseDTO.LoginResponseDTO> login(
        @Valid @RequestBody MemberRequestDTO.LoginRequestDTO request,
        HttpServletResponse response) {
        return ApiResponse.onSuccess(memberCommandFacade.login(request, response));
    }

    // 로그아웃
    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        memberCommandFacade.logout(request, response);
        return ApiResponse.onSuccess(null);
    }

    // 소셜 로그인 관련
    @Operation(summary = "소셜 로그인 성공 후 리다이렉트 (임시 컨트롤러)", description = """
        소셜 로그인 성공 후, 리다이렉트 되는 중간 경로입니다.

        - 실제 로그인 진입 경로는 `/oauth2/authorization/{provider}` (예: /oauth2/authorization/google) 이며,
        이 엔드포인트는 로그인 성공 후 JWT 토큰이 발급된 상태에서 호출됩니다.

        - 최초 로그인(회원가입)의 경우: `isProfileCompleted: false` 가 응답되고, 프로필이 이미 완료된 유저의 경우: `nickname` 이 응답됩니다.

        - 이 API는 프론트가 직접 호출하는 것이 아니라, 로그인 성공 후 자동으로 리다이렉트되는 경로입니다.
        """)
    @GetMapping(value = "/redirect/oauth2", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Map<String, Object>> handleSocialLoginRedirect(
        @RequestParam("isProfileCompleted") boolean isProfileCompleted,
        @RequestParam(value = "nickname", required = false) String nickname
    ) {
        if (isProfileCompleted) {
            return ApiResponse.onSuccess(
                Map.of("nickname", nickname));
        } else {
            return ApiResponse.onSuccess(
                Map.of("isProfileCompleted", false)
            );
        }
    }

    // 소셜 로그인 관련
    // GET /api/auth/oauth2/google - 소셜 로그인 (구글)
    // GET /api/auth/oauth2/kakao - 소셜 로그인 (카카오)
}
