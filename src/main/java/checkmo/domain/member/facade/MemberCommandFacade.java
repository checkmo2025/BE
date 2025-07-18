package checkmo.domain.member.facade;

import checkmo.domain.member.web.dto.MemberRequestDTO;
import checkmo.domain.member.web.dto.MemberResponseDTO;

/**
 * Member Domain Command Facade
 * Member 도메인의 Command(생성, 수정, 삭제) 관련 서비스들을 통합적으로 제공하는 Facade
 */
public interface MemberCommandFacade {

    //== MemberRegistrationCommandService ==//

    /**
     * 이메일 인증 요청 (내부용)
     *
     * @param email 인증할 이메일
     */
    void sendEmailVerification(String email);

    /**
     * 이메일 인증번호 확인 (내부용)
     *
     * @param request 이메일 인증 요청 DTO
     * @return 인증 성공 여부
     */
    boolean verifyEmailCode(MemberRequestDTO.EmailVerificationRequestDTO request);

    /**
     * 회원 가입 (내부용)
     *
     * @param request id,pw DTO
     * @return 회원 가입 응답 DTO
     */
    MemberResponseDTO.SignUpResponseDTO signUp(MemberRequestDTO.SignUpRequestDTO request);

    /**
     * 회원 추가 정보 입력 (내부용)
     *
     * @param request 추가 정보 DTO
     */
    void addAdditionalInfo(MemberRequestDTO.AdditionalInfoDTO request);

    //== MemberAuthenticationService ==//
    /**
     * 로그인 처리 (내부용)
     *
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     */
    void login(String email, String password);

    /**
     * 로그아웃 처리 (내부용)
     *
     * @param token 로그아웃할 JWT 토큰
     */
    void logout(String token);

    /**
     * 회원 계정 복구 (내부용)
     */
    void reactivateMember();


    //== MemberProfileCommandService ==//

    /**
     * 회원 프로필 정보 수정 (내부용)
     *
     * @param memberId 수정할 회원의 ID
     * @param request 수정할 프로필 정보 DTO
     * @return 수정된 회원 프로필 정보 DTO
     */
    MemberResponseDTO.MemberProfileResponseDTO updateMemberProfile(String memberId, MemberRequestDTO.MemberProfileUpdateRequestDTO request);

    /**
     * 회원 비밀번호 변경 (내부용)
     *
     * @param memberId 비밀번호를 변경할 회원의 ID
     * @param request 비밀번호 변경 정보 DTO
     */
    void updatePassword(String memberId, MemberRequestDTO.PasswordUpdateRequestDTO request);

    /**
     * 회원 계정 비활성화 (내부용)
     *
     * @param memberId 비활성화할 회원의 ID
     */
    void deactivateMember(String memberId);

    /**
     * 회원 계정 완전 삭제 (내부용)
     *
     * @param memberId 삭제할 회원의 ID
     */
    void deleteMember(String memberId);


    //== MemberFollowCommandService ==//

    /**
     * 특정 회원을 팔로우 (내부용/외부용) - Notification 도메인에서 사용
     *
     * @param memberId 팔로우할 회원의 ID
     * @param targetNickname 팔로우 대상 회원의 nickname
     */
    void followMember(String memberId, String targetNickname);

    /**
     * 특정 회원의 팔로우를 취소 (언팔로우) (내부용)
     *
     * @param memberId 언팔로우할 회원의 ID
     * @param targetNickname 팔로우 대상 회원의 nickname
     */
    void unfollowMember(String memberId, String targetNickname);

    /**
     * 특정 회원의 팔로잉를 취소 (언팔로잉) (내부용)
     *
     * @param memberId 언팔로우할 회원의 ID
     * @param targetNickname 팔로우 대상 회원의 nickname
     */
    void unfollowingMember(String memberId, String targetNickname);
}