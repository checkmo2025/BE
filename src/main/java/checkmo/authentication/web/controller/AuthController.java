package checkmo.authentication.web.controller;

import checkmo.authentication.AuthenticationEvent;
import checkmo.authentication.internal.service.AuthFacade;
import checkmo.authentication.internal.service.command.EmailVerificationCommandService;
import checkmo.authentication.web.dto.AuthRequestDTO;
import checkmo.authentication.web.dto.AuthResponseDTO;
import checkmo.common.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 API")
public class AuthController {

    private final EmailVerificationCommandService emailVerificationCommandService;
    private final AuthFacade authFacade;

    @Operation(summary = "이메일 인증번호 요청", description = "이메일 인증번호를 요청합니다. (SIGN_UP: 회원가입, UPDATE_EMAIL: 이메일 변경)")
    @Parameter(name = "email", description = "인증을 요청할 이메일 주소", required = true, example = "test@example.com")
    @PostMapping("/email-verification")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ApiResponse<String> sendEmailVerification(
            @RequestParam
            @Email(message = "유효한 이메일 주소를 입력해주세요")
            @NotBlank(message = "이메일은 필수입니다")
            String email,
            @RequestParam(defaultValue = "SIGN_UP") AuthenticationEvent.VerificationType type
    ) {
        emailVerificationCommandService.sendEmailVerification(email, type);
        return ApiResponse.onSuccess("인증번호가 이메일로 발송되었습니다.");
    }

    @Operation(summary = "이메일 인증번호 확인", description = "이메일 인증번호를 확인합니다.")
    @PostMapping("/email-verification/confirm")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ApiResponse<Boolean> verifyEmailCode(
            @Valid @RequestBody AuthRequestDTO.EmailVerification request
    ) {
        boolean isVerified = emailVerificationCommandService.verifyEmailCode(request);
        return ApiResponse.onSuccess(isVerified);
    }

    @Operation(summary = "회원가입", description = "이메일 인증 완료 후 회원가입을 진행합니다.")
    @PostMapping("/signup")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류입니다. 관리자에게 문의 바랍니다.")
    })
    public ApiResponse<AuthResponseDTO.SignUp> signUp(
            @Valid @RequestBody AuthRequestDTO.SignUp request,
            HttpServletResponse response
    ) {
        var signUpResponse = authFacade.signUp(request, response);
        return ApiResponse.onSuccess(signUpResponse);
    }

    @Operation(summary = "이메일/아이디 로그인", description = "이메일/아이디와 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 일치하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류입니다. 관리자에게 문의 바랍니다.")
    })
    public ApiResponse<String> login(
            @Valid @RequestBody AuthRequestDTO.Login request,
            HttpServletResponse response
    ) {
        authFacade.login(request, response);
        return ApiResponse.onSuccess("로그인에 성공했습니다.");
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @PostMapping("/logout")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    })
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authFacade.logout(request, response);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "비밀번호 재발급(임시 비밀번호 발송)", description = "등록된 이메일로 임시 비밀번호를 발송합니다.")
    @Parameter(name = "email", description = "비밀번호를 재발급받을 이메일 주소", required = true, example = "test@example.com")
    @PostMapping("/temp-password")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 이메일로 가입된 회원을 찾을 수 없습니다.")
    })
    public ApiResponse<String> sendTempPassword(
        @RequestParam
        @Email(message = "유효한 이메일 주소를 입력해주세요")
        @NotBlank(message = "이메일은 필수입니다")
        String email
    ) {
        emailVerificationCommandService.sendTempPassword(email);
        return ApiResponse.onSuccess("임시 비밀번호가 이메일로 발송되었습니다.");
    }
}
