package checkmo.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationAPI {

    /**
     * 현재 로그인 중인 AuthUser의 프로일을 완성 처리합니다.
     *
     * @param memberId 현재 로그인 중인 memberId
     */
    void completeProfile(Long memberId);

    /**
     * 특정 회원의 인증 계정을 비활성화하고 세션을 만료시킵니다.
     */
    void deactivateMember(Long memberId, HttpServletRequest request, HttpServletResponse response);

    /**
     * 특정 회원의 인증 관련 데이터(계정 정보, 권한, 토큰 등)를 완전히 삭제합니다.
     */
    void deleteAuthData(Long memberId);

    /**
     * 추가정보 입력 시 특정 회원의 닉네임을 저장합니다.
     */
    void updateNickname(Long memberId, String nickname);

    /**
     * 비밀번호를 변경합니다. authUser가 없거나 기존 비밀번호와 일치하면 에러 반환.
     *
     * @param memberId        현재 로그인 중인 memberId
     * @param currentPassword 기존 비밀번호
     * @param newPassword     새 비밀번호
     * @return 비밀번호 변경 성공 여부
     */
    boolean updatePassword(Long memberId, String currentPassword, String newPassword);


    /**
     * 이메일을 변강합니다.
     */
    void updateEmail(Long memberId, String currentEmail, String newEmail, String verificationCode);

    /**
     * 관리자 페이지 접근 권한 여부를 반환합니다.
     */
    boolean canAccessAdmin(Long memberId);

    String fetchProvider(Long memberId);
}
