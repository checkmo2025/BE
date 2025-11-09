package checkmo.member.web.controller;

import checkmo.common.apiPayload.ApiResponse;
import checkmo.member.MemberAPI;
import checkmo.member.internal.facade.MemberCommandFacade;
import checkmo.member.web.dto.MemberRequestDTO;
import checkmo.member.web.dto.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
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
    private final MemberAPI memberAPI;

    // 이메일 인증 요청
    @Operation(summary = "이메일 인증번호 요청", description = "회원가입 시 이메일 인증번호를 요청합니다.")
    @Parameter(name = "email", description = "인증을 요청할 이메일 주소", required = true, example = "test@example.com")
    @PostMapping("/email-verification")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
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
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ApiResponse<Boolean> verifyEmailCode(
            @Valid @RequestBody MemberRequestDTO.EmailVerificationRequestDTO request) {
        boolean isVerified = memberCommandFacade.verifyEmailCode(request);
        return ApiResponse.onSuccess(isVerified);
    }

    // 회원가입
    @Operation(summary = "회원가입", description = "이메일 인증 완료 후 회원가입을 진행합니다.")
    @PostMapping("/signup")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류입니다. 관리자에게 문의 바랍니다.")
    })
    public ApiResponse<MemberResponseDTO.SignUpResponseDTO> signUp(
            @Valid @RequestBody MemberRequestDTO.SignUpRequestDTO request,
            HttpServletResponse response) {
        return ApiResponse.onSuccess(memberCommandFacade.signUp(request, response));
    }

    // 회원 추가 정보 입력
    @Operation(summary = "회원 추가 정보 입력", description = "회원가입 후 추가 정보를 입력합니다.")
    @PostMapping("/additional-info")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 회원입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 회원을 찾을 수 없습니다.")
    })
    public ApiResponse<Void> addAdditionalInfo(
            @Valid @RequestBody MemberRequestDTO.AdditionalInfoDTO request) {
        memberCommandFacade.addAdditionalInfo(request);
        return ApiResponse.onSuccess(null);
    }

    // 닉네임 중복 확인
    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임 중복을 확인합니다.")
    @PostMapping("/check-nickname")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ApiResponse<Boolean> checkNickname(@RequestParam
                                              @NotBlank(message = "닉네임은 필수입니다")
                                              @Size(max = 6, message = "닉네임은 최대 6자까지 가능합니다") String nickname) {
        boolean isDuplicated = memberAPI.isNicknameDuplicated(nickname);
        return ApiResponse.onSuccess(isDuplicated);
    }

    // 이메일 로그인
    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 일치하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류입니다. 관리자에게 문의 바랍니다.")
    })
    public ApiResponse<MemberResponseDTO.LoginResponseDTO> login(
            @Valid @RequestBody MemberRequestDTO.LoginRequestDTO request,
            HttpServletResponse response) {
        return ApiResponse.onSuccess(memberCommandFacade.login(request, response));
    }

    // 로그아웃
    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @PostMapping("/logout")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    })
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        memberCommandFacade.logout(request, response);
        return ApiResponse.onSuccess(null);
    }
}
