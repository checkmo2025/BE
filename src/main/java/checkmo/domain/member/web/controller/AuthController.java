package checkmo.domain.member.web.controller;

import checkmo.apiPayload.ApiResponse;
import checkmo.domain.member.facade.MemberCommandFacade;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ApiResponse<Boolean> verifyEmailCode(@Valid @RequestBody MemberRequestDTO.EmailVerificationRequestDTO request) {
        boolean isVerified = memberCommandFacade.verifyEmailCode(request);
        return ApiResponse.onSuccess(isVerified);
    }

    // 회원가입
    @Operation(summary = "회원가입", description = "이메일 인증 완료 후 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ApiResponse<MemberResponseDTO.SignUpResponseDTO> signUp(@Valid @RequestBody MemberRequestDTO.SignUpRequestDTO request) {
        return ApiResponse.onSuccess(memberCommandFacade.signUp(request));
    }

    // 회원 추가 정보 입력
    @Operation(summary = "회원 추가 정보 입력", description = "회원가입 후 추가 정보를 입력합니다.")
    @PostMapping("/additional-info")
    public ApiResponse<Void> addAdditionalInfo(@Valid @RequestBody MemberRequestDTO.AdditionalInfoDTO request) {
        memberCommandFacade.addAdditionalInfo(request);
        return ApiResponse.onSuccess(null);
    }

    // 회원가입 관련

    // 닉네임 중복 확인
    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임 중복을 확인합니다.")
    @PostMapping("/check-nickname")
    public ApiResponse<Boolean> checkNickname(@RequestParam
                                              @NotBlank(message = "닉네임은 필수입니다")
                                              @Size(max = 6, message = "닉네임은 최대 6자까지 가능합니다") String nickname) {
        boolean isDuplicated = memberQueryFacade.isNicknameDuplicated(nickname);
        return ApiResponse.onSuccess(isDuplicated);
    }


    // POST /api/auth/additional-info - 회원 추가 정보 입력

    // 소셜 로그인 관련
    // GET /api/auth/oauth2/google - 소셜 로그인 (구글)
    // GET /api/auth/oauth2/kakao - 소셜 로그인 (카카오)

    // 일반 로그인/로그아웃 관련
    // POST /api/auth/login - 이메일 로그인
    // POST /api/auth/logout - 로그아웃
}
